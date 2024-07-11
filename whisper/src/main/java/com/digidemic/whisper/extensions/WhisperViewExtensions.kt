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

package com.digidemic.whisper.extensions

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.digidemic.whisper.Constants
import com.digidemic.whisper.Util
import com.digidemic.whisper.Whisper
import com.digidemic.whisper.WhisperWindow
import com.digidemic.whisper.extensions.LayoutSizingExtensions.linearLayoutParams
import com.digidemic.whisper.extensions.LayoutSizingExtensions.setEndingPaddingView
import com.digidemic.whisper.extensions.LayoutSizingExtensions.setMargins
import com.digidemic.whisper.extensions.PixelDensityExtensions.toPixelDensityUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Common extension functions for Whisper to consume internally that are related to Whisper's inner workings.
 */
internal object WhisperViewExtensions {

    /** Used when [Whisper.GlobalConfig.timeoutOnlyForOldestWhisper] is true for auto-removing the oldest visible Whisper when timer expires. */
    @Volatile
    private var whisperAutoCloseJob: Job = Job().also { it.cancel() }

    /** Cancels the Whisper auto-remove timer. */
    internal fun cancelWhisperAutoCloseJob() {
        whisperAutoCloseJob.cancel()
    }

    /**
     * Starts the Whisper removal process from the Whisper window (sole layout that houses all Whisper views).
     * Begins by fading out the Whisper until fully invisible.
     * Next, the Whisper is removed from the Window where the remaining Whispers are
     * reorganized, made visible, and updating the auto-removing timer.
     *
     * @param activity active Activity.
     * @param cancelTimer if the current auto-removing timer should be canceled
     */
    fun View.removeWhisper(activity: Activity, cancelTimer: Boolean) {
        // Start the fade out animation and do not proceed with the Whisper removal until the fade out is complete.
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = Whisper.GlobalConfig.animationTransitionDuration
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                // Fade out just completed, Whisper View still exists but is invisible. Begin the removal process.

                /*
                Before removing the Whisper view from the Whisper window (layout that houses all Whisper views),
                check if other Whispers are currently displayed (or invisible queued to be displayed).
                If so, the remaining Whispers will need to be reorganized visually causing a set of transition animations.
                The Whisper window always has a height of WRAP_CONTENT because it appears on top of the users current activity.
                This should not take up more space than needed as users cannot tap something on screen that is behind the Whisper window.
                This can cause remaining Whispers to briefly and partially appear visual cut off while transitioning after the current Whisper is removed from the window.
                To address this keeping WRAP_CONTENT from shrinking the window layout too quickly,
                an invisible view [whisperEndPadding] exists at the end of the layout with a dynamic height that changes temporarily to account for proper movement animations.
                The Whisper set to be removed has its height extracted and applied to the in invisible view for the animation duration before reverting back to 0.
                 */
                val elmCount = Whisper.activeWhisperCount()
                if (elmCount > 1) {
                    val invisibleTransitionPadding =
                        this@removeWhisper.height + activity.toPixelDensityUnit(
                            Whisper.GlobalConfig.displaySpace
                        )
                    if(elmCount <= Whisper.GlobalConfig.maxVisible) {
                        invisibleTransitionPadding
                            .setEndingPaddingView()
                    } else {
                        // Past max Whisper to display on screen, set only half the intended height
                        //   to keep different sized whispers from distorting the transition.
                        (invisibleTransitionPadding / 2.0)
                            .roundToInt()
                            .setEndingPaddingView()
                    }
                }

                // Remove Whisper view from Whisper window (single layout that houses all Whispers being displayed).
                (this@removeWhisper.parent as? ViewGroup)?.removeView(this@removeWhisper)

                /*
                 The now removed Whisper may not have closed due to the auto-removing timer expiring or was the Whisper the timer was observing.
                 If multiple Whispers exist on screen, a user can tap or call [Whisper.remove] on any Whisper at any time (With
                 the proper configurations set like [Whisper.GlobalConfig.timeoutOnlyForOldestWhisper] and [Whisper.GlobalConfig.tapToDismiss] enabled).
                 In these cases, it may not be the right time to cancel the active timer.
                 */
                if (cancelTimer) {
                    cancelWhisperAutoCloseJob()
                }

                /*
                 Views in the Whisper window (single layout for all Whispers) count is now different.
                 Loop through updated list of active Whispers to perform various actions where needed.
                 */
                var resetTimer = false
                Util.traverseWhispers { elementTagCount, _, view ->
                    // Update the margins of the oldest Whisper (top or bottom most visible Whisper depending on [Whisper.GlobalConfig.sortOrder]).
                    //  This Whisper, due to its position on screen, should not have a [Whisper.GlobalConfig.displaySpace] applied to its margins.
                    if (elementTagCount == 1) {
                        val margin = when (Whisper.GlobalConfig.sortOrder) {
                            Whisper.Enums.SortOrder.ABOVE -> view.linearLayoutParams.bottomMargin
                            Whisper.Enums.SortOrder.BELOW -> view.linearLayoutParams.topMargin
                        }
                        if(margin > Constants.DEFAULT_WHISPER_MARGIN) {
                            view.setMargins(Constants.DEFAULT_WHISPER_MARGIN)
                        }
                    }

                    // If [Whisper.GlobalConfig.maxVisible] is reached, determine what Whispers should now be visible.
                    view.visibility = if (elementTagCount <= Whisper.GlobalConfig.maxVisible) View.VISIBLE else View.GONE

                    /*
                    A Whisper can have a set duration of 0 meaning the auto-removal timeout never applies to it.
                    Because of this, the oldest Whisper does not always mean it is the next Whisper that should be affected by the timeout.
                    Check the duration of the Whisper being iterated on making sure it has a duration set before attempting to refresh the timeout.
                    If a timeout is already active for another Whisper and [Whisper.GlobalConfig.timeoutOnlyForOldestWhisper] is enabled,
                    [refreshTimer] will not overwrite the current active timeout.
                     */
                    if (!resetTimer) {
                        val duration = view.extractDurationFromTag(0)
                        if (duration > 0) {
                            resetTimer = true
                            view.refreshTimer(activity)
                        }
                    }
                }

                /*
                With the current Whisper now removed, the remaining displaying Whispers are now reorganizing,
                animating for the [Whisper.GlobalConfig.animationTransitionDuration] duration.
                The invisible view, [whisperEndPadding], at the end of the Whisper window may have its height defined past 0 to ensure no Whispers
                briefly and partially appear cut off for the transition.
                Once the transition is complete, the invisible view's height is reset back to 0 as done here.
                CoroutineScope(Dispatchers.Main) (or runOnUiThread) is needed here as device will become unresponsive to any touch for the duration.
                 */
                CoroutineScope(Dispatchers.Main).launch {
                    delay(Whisper.GlobalConfig.animationTransitionDuration)
                    0.setEndingPaddingView()
                    this.coroutineContext.cancel()
                }
            }
        })

        // Start animation to remove view. onAnimationEnd() will be called when animation completes to then fully remove the Whisper view.
        this.startAnimation(fadeOut)
    }

    /**
     * Add newly created Whisper view to Whisper window (sole layout that houses all Whisper views).
     */
    fun View.addWhisper() {
        /*
        Should the newly added Whisper be visible or invisible upon being added.
        If passed the [Whisper.GlobalConfig.maxVisible] value, the Whisper will be added to the Whisper window but be invisible.
         */
        Util.traverseWhispers { elementTagCount, _, view ->
            if (elementTagCount > Whisper.GlobalConfig.maxVisible) {
                view.visibility = View.GONE
            } else if (elementTagCount == Whisper.GlobalConfig.maxVisible) {
                this.visibility = View.GONE
            }
        }

        // Relative to the existing Whispers being displayed, determine where the newly created Whisper should be placed.
        try {
            when (Whisper.GlobalConfig.sortOrder) {
                Whisper.Enums.SortOrder.ABOVE ->
                    WhisperWindow.windowLayout?.addView(this, 0)
                Whisper.Enums.SortOrder.BELOW -> {
                    // One before the last element of the Whisper window to account for the invisible view [whisperEndPadding]
                    //   which needs to always be at the end of the window. See [removeWhisper] for more information.
                    val index = (WhisperWindow.windowLayout?.childCount ?: 1) - 1
                    WhisperWindow.windowLayout?.addView(this, index)
                }
            }
        } catch (_: IndexOutOfBoundsException) {
            try {
                WhisperWindow.windowLayout?.addView(this, 0)
            } catch (_: Exception) {
                // Exception may include "java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.".
                //   If this or any other failure case, skip adding this view.
            }
        } catch (_: Exception) {
            // Exception may include "java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.".
            //   If this or any other failure case, skip adding this view.
        }

        // Revert the invisible view height for transition animation (in the event the height is not 0).
        //   The newly created Whisper temporarily also acts as the layout expansion that is sometimes needed in transitions like when a Whisper is removed.
        0.setEndingPaddingView()
    }

    /**
     * Start and auto-removing timer to auto-close a Whisper which the timer expires.
     * Different conditions depending on [Whisper.GlobalConfig.timeoutOnlyForOldestWhisper] and if a timeout is already active.
     */
    fun View.refreshTimer(activity: Activity) {
        // If [timeoutOnlyForOldestWhisper] is false, create an additional timeout.
        if (!Whisper.GlobalConfig.timeoutOnlyForOldestWhisper) {
            CoroutineScope(Dispatchers.Main).launch {
                val duration = extractDurationFromTag(Whisper.GlobalConfig.durationDisplayMinimum)
                delay(duration)
                this@refreshTimer.removeWhisper(activity, false)
                this.coroutineContext.cancel()
            }

        // If [timeoutOnlyForOldestWhisper] is true, only refresh the sole timeout if it is not already running.
        } else if (!whisperAutoCloseJob.isActive) {
            whisperAutoCloseJob = CoroutineScope(Dispatchers.Main).launch {
                val duration = extractDurationFromTag(Whisper.GlobalConfig.durationDisplayMinimum)
                delay(duration)
                this@refreshTimer.removeWhisper(activity, true)
            }
        } else { /* do nothing */ }
    }
}