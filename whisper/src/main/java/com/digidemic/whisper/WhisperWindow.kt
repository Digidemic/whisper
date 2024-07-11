/**
 * Whisper v1.0.0 - https://github.com/Digidemic/whisper
 * (c) 2024 DIGIDEMIC, LLC - All Rights Reserved
 * Whisper developed by Adam Steinberg of DIGIDEMIC, LLC
 * License: Apache License 2.0
 *
 * ====
 *
 * Copyright 2024 DIGIDEMIC, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digidemic.whisper

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.digidemic.whisper.extensions.LayoutSizingExtensions.layoutParamWidth
import com.digidemic.whisper.extensions.LayoutSizingExtensions.linearLayoutParams
import com.digidemic.whisper.extensions.LayoutSizingExtensions.locationIsTop
import com.digidemic.whisper.extensions.LayoutSizingExtensions.setEndingPaddingView
import com.digidemic.whisper.extensions.PixelDensityExtensions.toPixelDensityUnit
import com.digidemic.whisper.extensions.StatusBarExtensions.statusBarHeight
import com.digidemic.whisper.extensions.WhisperViewExtensions
import com.digidemic.whisper.extensions.WhisperViewExtensions.addWhisper
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Inherited by [BuildNewWhisper] to be used as the setup for the Whisper window (layout that houses all Whispers),
 * Apply whisper.yaml settings, and perform any other overall setup.
 * Only one [whisperWindow] (the [PopupWindow]) can exist at a time.
 * Because of this, comparing the Activity hash for each Whisper that is created is necessary to determine
 * if the current Whisper caches and processes need to be cleared and setup of the Whisper window needs to be reset.
 */
@SuppressLint("DiscouragedApi", "InternalInsetResource", "InflateParams")
internal abstract class WhisperWindow(private val activity: Activity) {

    init {
        // Whisper core setup. Only needs to run once per Activity instance that is creating a Whisper for the first time.
        //   Double-check locking to reduce overhead of synchronization.
        if (setupWhisperWindow()) {
            synchronized(syncLock) {
                if (setupWhisperWindow()) {

                    // Used to determine if Activity has already been set up to use Whisper.
                    //  Overwrite the previous activity using Whisper with the current one.
                    activityHash.set(activity.hashCode())

                    // Apply yaml to [GlobalConfig] and all profiles if YAML has not been applied anytime in the lifecycle of the app.
                    YamlConfig.applyIfNeverRan(activity)

                    // Whisper may have been setup with a previous Activity until now.
                    //   Clean up Whisper processes and caches that may still be tied to that Activity.
                    whisperWindowCleanUp()

                    // Instantiate the content view to be passed when instantiating the PopupWindow.
                    val windowContentView = LinearLayout(activity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = linearLayoutParams
                    }

                    // Create the PopupWindow (Whisper window) which will act as the sole layout all house all Whisper views.
                    //   Width only MATCH_PARENT when [Whisper.GlobalConfig.positionOnScreen] is [TOP_FULL].
                    //   This is needed to create full width Whisper views (if configured to do so).
                    whisperWindow = PopupWindow(
                        windowContentView,
                        layoutParamWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        // [post] Address the following exception which can be thrown if Whisper is called too early in the Activity lifecycle:
                        //   android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
                        activity.window.decorView.post {
                            try {
                                // Show PopupWindow.
                                showAtLocation(
                                    activity.window.decorView,
                                    Whisper.GlobalConfig.positionOnScreen.gravity,
                                    Whisper.GlobalConfig.offset.x.toPixelDensityUnit(),
                                    Whisper.GlobalConfig.offset.y.toPixelDensityUnit() + paddingForStatusBarHeight()
                                )
                            } catch (_: Exception) {
                                /*
                                 [showAtLocation] can fail for a number of reasons.
                                 If it does, Whisper has not properly setup for the Activity passed.
                                 Because setup is incomplete, revert the setup flag and clean up any active Whisper processes and caches.
                                 */
                                initSetup.set(false)
                                whisperWindowCleanUp()
                            }
                        }
                    }

                    // Sets animation of Whisper movement for when one closes and another moves to adjust.
                    setMovementTransition()

                    /*
                    With Whisper window created, add the first and always present invisible view, [whisperEndPadding], to the layout.
                    This view is never removed for this instance of Whisper window because it is used in assisting animation.
                    The invisible view has a height of 0 unless an element was remove/whispers need to rearrange on screen.
                     See [WhisperViewExtensions.removeWhisper] for more information on the invisible view.
                    */
                    if (windowLayout?.findViewById<View>(R.id.whisperEndPadding) == null) {
                        val layoutInflater = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        layoutInflater
                            .inflate(R.layout.invisible_end_padding, null)
                            .addWhisper()

                        0.setEndingPaddingView()
                    }

                    // Whisper window setup without error for the passed Activity.
                    //   Future Whispers created for this Activity will not have to go through the same setup (as long as the Activity does not change).
                    initSetup.set(true)
                }
            }
        }

        /*
        While the Whisper window setup only needs to be performed once per activity (every Whisper for that activity after will skip the setup),
        some configurable settings will not appear to have an effect after updating.
        Settings like the x/y offset, that applied only when the Whisper window PopupWindow was previously instantiated, will not update if the fields changed after.
        To address this, the following condition checks if specific fields like the x/y offset changed.
        This will be checked each time a Whisper is created, regardless if the initial setup for the Activity is already complete.
        If the condition is used, all fields that normally have no effect after PopupWindow instantiation are applied.
         */
        if(Whisper.GlobalConfig.updateWhisperWindowLocation.get() && initSetup.get() && whisperWindow?.isShowing == true) {
            Whisper.GlobalConfig.updateWhisperWindowLocation.set(false)
            try {
                whisperWindow?.update(
                    Whisper.GlobalConfig.offset.x.toPixelDensityUnit(),
                    Whisper.GlobalConfig.offset.y.toPixelDensityUnit() + paddingForStatusBarHeight(),
                    layoutParamWidth,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    true
                )
                setMovementTransition()
            } catch (_: Exception) { /* do nothing */ }
        } else {
            Whisper.GlobalConfig.updateWhisperWindowLocation.set(false)
        }
    }

    /**
     * Helper extension function consumed here in [WhisperWindow] and [BuildNewWhisper] to call
     * activity.toPixelDensityUnit by containing the [Activity] requiring one less variable when
     * calling the function.
     */
    internal fun Int.toPixelDensityUnit(): Int = activity.toPixelDensityUnit(this)

    /**
     * Determines if [WhisperWindow] init (Whisper core setup) needs to run.
     * Several factor determine if the setup needs to be reran:
     *   - If the initial setup was never completed ([initSetup] false).
     *   - If [whisperWindow] ([PopupWindow]) is not showing.
     *   - If the passed [activity]'s hashcode is different than the current expected hashcode ([activityHash]).
     */
    private fun setupWhisperWindow() = !initSetup.get() || whisperWindow?.isShowing != true || activity.hashCode() != activityHash.toInt()

    /** Animation of Whisper movement when one closes and another moves to adjust. */
    private fun setMovementTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowLayout?.layoutTransition = LayoutTransition().apply {
                setDuration(Whisper.GlobalConfig.animationTransitionDuration)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
                }
            }
        }
    }

    /**
     * Returns the status bar height if [Whisper.GlobalConfig.Offset.additionalOffsetForStatusBar]
     * and [locationIsTop] is true. Otherwise 0 is returned.
     * Value is applied to the Y Offset in setting Whisper window's [PopupWindow.showAtLocation] and/or [PopupWindow.update].
     */
    private fun paddingForStatusBarHeight(): Int =
        if (Whisper.GlobalConfig.offset.additionalOffsetForStatusBar && locationIsTop) {
            activity.statusBarHeight()
        } else {
            0
        }

    companion object {
        /**
         * Set true after first call to [WhisperWindow] for the passed activity completes without failure.
         * Used as a conditional flag for initial setup for the specific Activity.
         */
        private val initSetup = AtomicBoolean(false)

        /** Activity hash which the current Whisper window has been placed to. */
        private val activityHash = AtomicInteger(0)

        /** Single PopupWindow which all Whisper views are added to. */
        @Volatile
        private var whisperWindow: PopupWindow? = null

        /** synchronized lock for WhisperWindow init */
        private val syncLock = Any()

        /** Returns the Layout of the Whisper window (PopupWindow). */
        val windowLayout: LinearLayout?
            get() = whisperWindow?.contentView as? LinearLayout

        /**
         * Only called from the public facing [Whisper.finish].
         * [Whisper.finish] should be added to the onDestroy function of every activity that may use Whisper.
         * Failure to do so may cause a "android.view.WindowLeaked" error to occur when the activity is destroyed (if that activity created any Whispers).
         *
         * @param activity is required for an edge case when multiple Activities are active and each Activity has [Whisper.finish] in their onDestroy.
         *   In the event Activity A having Whispers being displayed then opens/navigates to Activity B > then shortly after, B navigates back to A > B's onDestroy to be called but Activity A can continue to display its Whispers.
         *   Without this [activity] param, closing Activity B would remove all Whispers from Activity A when the user returned back from their brief departure from A.
         */
        fun finish(activity: Activity) {
            if(activity.hashCode() == activityHash.get()) {
                whisperWindowCleanUp()
            }
        }

        /**
         * Clean up Whisper processes and caches.
         * Function should be called anytime a prior Activity that used Whisper at anytime no longer
         * will be creating a Whisper.
         * This includes:
         *   - when an Activity that may use Whisper is being destroyed (onDestroy).
         *   - a new Activity hash is opening a Whisper for the first time (to clear out the previous processes).
         *   - Initial setup of a Whisper Window, [PopupWindow], failed (to ensure proper clean up / any processes running end).
         */
        private fun whisperWindowCleanUp() {
            /*
            Among many reasons, an edge case why canceling the auto-remove timeout job is needed:
              1. If Activity A opens/navigates to Activity B (and A remains an active/non-destroyed Activity)
              2. B creates a whisper
              3. While that Whisper is still being displayed on B, B navigates back to A.
              4. A then creates a Whisper
              5. Whisper on A will never auto-close because the [whisperTimerJob] static variable never finished from Activity B.
              Thus canceling the job here fixes the above use case with the frequency of when and how often this function is called.
             */
            WhisperViewExtensions.cancelWhisperAutoCloseJob()

            // If true from a prior interaction, set to false as next time a Whisper is created,
            //   it will need to go through the Whisper Window setup process.
            Whisper.GlobalConfig.updateWhisperWindowLocation.set(false)

            // If a Whisper window has not been disabled when an Activity is being destroyed,
            //   the following error may appear in the logs: android.view.WindowLeaked.
            whisperWindow?.dismiss()
        }
    }
}