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

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import com.digidemic.whisper.Constants.DEFAULT_ANIMATION_TRANSITION_DURATION
import com.digidemic.whisper.Constants.DEFAULT_ADDITIONAL_OFFSET_FOR_STATUS_BAR
import com.digidemic.whisper.Constants.DEFAULT_CRITICAL_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_CRITICAL_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_DEBUG_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_DEBUG_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_DEFAULT_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_DEFAULT_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_DISPLAY_SPACE
import com.digidemic.whisper.Constants.DEFAULT_DURATION_DISPLAY_MINIMUM
import com.digidemic.whisper.Constants.DEFAULT_DURATION_DISPLAY_MAXIMUM
import com.digidemic.whisper.Constants.DEFAULT_TIMEOUT_LENGTH_PER_CHARACTER
import com.digidemic.whisper.Constants.DEFAULT_ERROR_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_ERROR_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_FATAL_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_FATAL_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_INFO_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_INFO_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_MAX_VISIBLE
import com.digidemic.whisper.Constants.DEFAULT_OFFSET_X
import com.digidemic.whisper.Constants.DEFAULT_OFFSET_Y
import com.digidemic.whisper.Constants.DEFAULT_TIMEOUT_ONLY_FOR_OLDEST_WHISPER
import com.digidemic.whisper.Constants.DEFAULT_TAP_TO_DISMISS
import com.digidemic.whisper.Constants.DEFAULT_TRACE_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_TRACE_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_WARN_PROFILE_BACKGROUND_COLOR
import com.digidemic.whisper.Constants.DEFAULT_WARN_PROFILE_BORDER_COLOR
import com.digidemic.whisper.Constants.DEFAULT_WHISPER_ID
import com.digidemic.whisper.Constants.DEFAULT_YAML_FILE_NAME
import com.digidemic.whisper.Constants.defaultPixelDensityUnit
import com.digidemic.whisper.Constants.defaultSortOrder
import com.digidemic.whisper.Constants.defaultPositionOnScreen
import com.digidemic.whisper.Whisper.GlobalConfig.sortOrder
import com.digidemic.whisper.extensions.BuildExtensions.isProperBuildWithProfile
import com.digidemic.whisper.extensions.WhisperViewExtensions.removeWhisper
import com.digidemic.whisper.extensions.extractDurationFromTag
import com.digidemic.whisper.extensions.greaterThanZero
import com.digidemic.whisper.extensions.zeroOrGreater
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Highly configurable, unintrusive, non-model, multi-displaying, auto-positioning, auto-removing, suite of popup messages for Android.
 */
object Whisper {

    object Profiles {
        /** Default profile settings. To use, call function [Whisper.default] or [Whisper.invoke]. */
        val default = ProfileTemplate(
            DEFAULT_DEFAULT_PROFILE_BACKGROUND_COLOR,
            DEFAULT_DEFAULT_PROFILE_BORDER_COLOR,
        )

        /** Info profile settings. To use, call function [Whisper.info]. */
        val info = ProfileTemplate(
            DEFAULT_INFO_PROFILE_BACKGROUND_COLOR,
            DEFAULT_INFO_PROFILE_BORDER_COLOR,
        )

        /** Warn profile settings. To use, call function [Whisper.warn]. */
        val warn = ProfileTemplate(
            DEFAULT_WARN_PROFILE_BACKGROUND_COLOR,
            DEFAULT_WARN_PROFILE_BORDER_COLOR,
        )

        /** Error profile settings. To use, call function [Whisper.error]. */
        val error = ProfileTemplate(
            DEFAULT_ERROR_PROFILE_BACKGROUND_COLOR,
            DEFAULT_ERROR_PROFILE_BORDER_COLOR,
        )

        /** Fatal profile settings. To use, call function [Whisper.fatal]. */
        val fatal = ProfileTemplate(
            DEFAULT_FATAL_PROFILE_BACKGROUND_COLOR,
            DEFAULT_FATAL_PROFILE_BORDER_COLOR,
        )

        /** Critical profile settings. To use, call function [Whisper.critical]. */
        val critical = ProfileTemplate(
            DEFAULT_CRITICAL_PROFILE_BACKGROUND_COLOR,
            DEFAULT_CRITICAL_PROFILE_BORDER_COLOR,
        )

        /** Trace profile settings. To use, call function [Whisper.trace].
         * Only if the app is running in a non-release build will Whispers using this profile be displayed when called.
         */
        val trace = ProfileTemplate(
            DEFAULT_TRACE_PROFILE_BACKGROUND_COLOR,
            DEFAULT_TRACE_PROFILE_BORDER_COLOR,
        )

        /** Debug profile settings. To use, call function [Whisper.debug]
         *  Only if the app is running in a non-release build will Whispers using this profile be displayed when called.
         */
        val debug = ProfileTemplate(
            DEFAULT_DEBUG_PROFILE_BACKGROUND_COLOR,
            DEFAULT_DEBUG_PROFILE_BORDER_COLOR,
        )
    }

    /**
     * All global configurable settings can be found and configured here.
     * See [Profiles] for profile editable configurations specific to each profile.
     * All [GlobalConfig] and [Profiles] configurations can also be set in the whisper.yaml file.
     * The whisper.yaml file is recommended to use for configuration modification over code changes to [GlobalConfig] and [Profiles].
     */
    object GlobalConfig {
        /**
         * Flag observed only by [WhisperWindow] to allow window specific fields to be altered after
         * the window has been created for the activity.
         */
        internal val updateWhisperWindowLocation = AtomicBoolean(false)

        /** YAML file name expected in /assets/ directory to be used for loading in all custom configurations. */
        var yamlFileName = DEFAULT_YAML_FILE_NAME

        /** The pixel density to use for all Whisper UI configuration fields (other than font size). */
        var pixelDensityUnit: Enums.PixelDensity = defaultPixelDensityUnit
            set(value) {
                field = value
                updateWhisperWindowLocation.set(true)
            }

        /**
         * Location on screen where to display Whispers.
         * Multiple Whispers visible at once will be positioned relative to this location in the order defined by [sortOrder].
         * Further adjustments can be made with [Offset.x], [Offset.y], and [Offset.additionalOffsetForStatusBar].
         */
        var positionOnScreen = defaultPositionOnScreen
            set(value) {
                field = value
                updateWhisperWindowLocation.set(true)
            }

        /**
         * Relative position to display newer Whispers when more than one are visible on screen at once.
         * See [positionOnScreen] for Whisper placement.
         */
        var sortOrder = defaultSortOrder

        /**
         * Maximum number of Whispers visible on screen at once.
         * If more Whispers exist than the defined value, excess Whispers will be invisible until the space is available.
         * For no maximum, set to 0.
         */
        var maxVisible = DEFAULT_MAX_VISIBLE
            get() = field.greaterThanZero(DEFAULT_MAX_VISIBLE)

        /** Space between each Whisper if multiple are displaying at once. */
        var displaySpace = DEFAULT_DISPLAY_SPACE
            get() = field.zeroOrGreater(DEFAULT_DISPLAY_SPACE)

        /**
         * In milliseconds, the added auto-timeout length per character in the Whisper message.
         * Value here in combination of text length determines the dynamic auto-timeout of each Whisper.
         * Range will always be within [durationDisplayMinimum] and [durationDisplayMaximum] values.
         * Auto-timeout will not be used for individual Whispers with an optional timeout value passed in.
         *
         * How [timeoutLengthPerCharacter] default value was calculated:
         * The average reading speed for learning in English is between 100-200 wpm.
         * The average word in the English language is 4.7 characters.
         * If using 160 wpm and 5 characters per word (including spaces):
         * (160 * 5) / 60 = 13.33 char/sec or 1/13.33 for every 1 character = 0.075s or 75ms.
         */
        var timeoutLengthPerCharacter = DEFAULT_TIMEOUT_LENGTH_PER_CHARACTER
            get() = field.greaterThanZero(DEFAULT_TIMEOUT_LENGTH_PER_CHARACTER)

        /**
         * In milliseconds, the minimum duration a Whisper will be visible for before timing out.
         * Applies if [timeoutLengthPerCharacter] value computes too short of a duration for auto-timeouts.
         * Applies to individual Whispers with an optional timeout value passed in.
         */
        var durationDisplayMinimum = DEFAULT_DURATION_DISPLAY_MINIMUM
            get() = field.zeroOrGreater(DEFAULT_DURATION_DISPLAY_MINIMUM)

        /**
         * In milliseconds, the maximum duration a Whisper will be visible for before timing out.
         * Applies if [timeoutLengthPerCharacter] value computes too long of a duration for auto-timeouts.
         * Does not apply to individual Whispers with an optional timeout value passed in.
         */
        var durationDisplayMaximum = DEFAULT_DURATION_DISPLAY_MAXIMUM
            get() = field.zeroOrGreater(DEFAULT_DURATION_DISPLAY_MAXIMUM)

        /**
         * In milliseconds, the value of all animation transitions.
         * This includes fading in, fading out, and rearranging.
         * For no animation, set to 0.
         */
        var animationTransitionDuration = DEFAULT_ANIMATION_TRANSITION_DURATION
            get() = field.zeroOrGreater(DEFAULT_ANIMATION_TRANSITION_DURATION)
            set(value) {
                field = value
                updateWhisperWindowLocation.set(true)
            }

        /**
         * When a timeout applies to a Whisper, timer is only activated to the oldest visible Whisper.
         * Once the oldest Whisper is dismissed, the next visible Whisper's timer (set for an auto-close timeout) will begin.
         * False will start a timer for each visible Whisper immediately (except for Whispers set to never timeout).
         */
        var timeoutOnlyForOldestWhisper = DEFAULT_TIMEOUT_ONLY_FOR_OLDEST_WHISPER

        /** Ability to dismiss a Whisper immediately by tapping it. */
        var tapToDismiss = DEFAULT_TAP_TO_DISMISS

        /**
         * Whisper offsets on screen.
         * See [positionOnScreen] and [sortOrder] for other placement options.
         */
        val offset: Offset = Offset()

        class Offset {
            /** Adds an X offset to [positionOnScreen]. */
            var x = DEFAULT_OFFSET_X
                get() = field.zeroOrGreater(DEFAULT_OFFSET_X)
                set(value) {
                    field = value
                    updateWhisperWindowLocation.set(true)
                }

            /** Adds a Y offset to [positionOnScreen]. */
            var y = DEFAULT_OFFSET_Y
                get() = field.zeroOrGreater(DEFAULT_OFFSET_Y)
                set(value) {
                    field = value
                    updateWhisperWindowLocation.set(true)
                }

            /**
             * If the device's status bar is visible, applying a small [Offset.y] may appear to have
             * no effect on the offset (if the [positionOnScreen] is intended to be below the status bar).
             * By setting true, the device's status bar height is calculated and added to the [Offset.y] so it is properly accounted for.
             */
            var additionalOffsetForStatusBar = DEFAULT_ADDITIONAL_OFFSET_FOR_STATUS_BAR
                set(value) {
                    field = value
                    updateWhisperWindowLocation.set(true)
                }
        }

        /**
         * This function is unnecessary to call for most use cases.
         * If a whisper.yaml config is present, this function applies it to your Whisper settings only if whisper.yaml has not already been applied prior in the lifecycle of the app.
         * whisper.yaml automatically applies to your settings when calling [Whisper] for the first time in your app's lifecycle.
         * The only time this function may need to be called is if there were dynamic code value changes made to [GlobalConfig] or [Profiles] prior to calling your first whisper.
         * This so whisper.yaml does not overwrite those programmatic changes.
         *
         * Note: it is not recommended to ever make [GlobalConfig] or [Profiles] changes programmatically.
         * Recommended is to only use whisper.yaml for configuration changes.
         */
        fun applyYamlIfNeverRan(activity: Activity) {
            YamlConfig.applyIfNeverRan(activity)
        }

        /**
         * This function is unnecessary to call for most use cases.
         * If a whisper.yaml config is present, this function will overwrite any config changes made.
         * This includes if prior changes were made programmatically to [GlobalConfig] or [Profiles].
         * Or if whisper.yaml was already applied prior in the life of your app.
         * whisper.yaml automatically applies to your settings when calling [Whisper] for the first time in your app's lifecycle.
         * The only time this function may be needed is if dynamic code value changes were made to[GlobalConfig] or [Profiles] that now need to be reverted.
         * This so whisper.yaml overwrites those programmatic changes no longer needed.
         *
         * Note: it is not recommended to ever make [GlobalConfig] or [Profiles] changes programmatically.
         * Recommended is to only use whisper.yaml for configuration changes.
         */
        fun applyYaml(activity: Activity) {
            YamlConfig.apply(activity)
        }
    }

    /** All Enums used by [GlobalConfig] and [Profiles] */
    object Enums {
        /**
         * Options for the pixel density to use for all Whisper UI configuration fields (other than font size).
         * Used as value options for [GlobalConfig.pixelDensityUnit].
         */
        enum class PixelDensity {
            /** Density Independent Pixels */
            DP,
            /** Pixels */
            PX,
        }

        /**
         * Options for the location on screen where to display Whispers.
         * Used as value options for [GlobalConfig.positionOnScreen].
         * Multiple Whispers visible at once will be positioned relative to this location in the order defined by [sortOrder].
         * Further adjustments can be made with [GlobalConfig.Offset.x], [GlobalConfig.Offset.y], and [GlobalConfig.Offset.additionalOffsetForStatusBar].
         */
        @SuppressLint("RtlHardcoded")
        enum class PositionOnScreen(val gravity: Int) {
            /** Push Whispers to the top center of the screen. */
            TOP_CENTER(Gravity.TOP),
            /** Push Whispers to the top left of the screen. */
            TOP_LEFT(Gravity.TOP or Gravity.LEFT),
            /** Push Whispers to the top right of the screen. */
            TOP_RIGHT(Gravity.TOP or Gravity.RIGHT),
            /** Push Whispers to the top start of the screen. */
            TOP_START(Gravity.TOP or Gravity.START),
            /** Push Whispers to the top end of the screen. */
            TOP_END(Gravity.TOP or Gravity.END),
            /** Push Whispers to the top of the screen and extending the Whisper width to each side of the screen. */
            TOP_FULL(Gravity.TOP),
        }

        /**
         * Options for the relative position to display newer Whispers when more than one are visible on screen at once.
         * Used as value options for [GlobalConfig.sortOrder]
         * Also see [GlobalConfig.positionOnScreen] for Whisper placement.
         */
        enum class SortOrder {
            /** Newly created Whispers will be displayed above the all other visible Whispers. */
            ABOVE,
            /** Newly created Whispers will be displayed below the all other visible Whispers. */
            BELOW,
        }

        /**
         * Options for the horizontal alignment of a Whisper's text.
         * Used as value options for [ProfileTemplate.DesignTemplate.TextTemplate.gravity].
         */
        @SuppressLint("RtlHardcoded")
        enum class TextGravity(val gravity: Int) {
            /** Push the text to the left of the Whisper. */
            LEFT(Gravity.LEFT),
            /** Push the text to the right of the Whisper. */
            RIGHT(Gravity.RIGHT),
            /** Push the text to the center of the Whisper. */
            CENTER(Gravity.CENTER),
            /** Push the text to the start of the Whisper  */
            START(Gravity.START),
            /** Push the text to the end of the Whisper  */
            END(Gravity.END),
        }

        /**
         * Options for if background is a solid color or a type of gradient.
         * Used as value options for [ProfileTemplate.DesignTemplate.BackgroundTemplate.type].
         */
        enum class BackgroundType(val gradient: Int) {
            /** Single color */
            SOLID(-1),
            /** Gradient is linear */
            GRADIENT_LINEAR(GradientDrawable.LINEAR_GRADIENT),
            /** Gradient is circular */
            GRADIENT_RADIAL(GradientDrawable.RADIAL_GRADIENT),
            /** Gradient is a sweep */
            GRADIENT_SWEEP(GradientDrawable.SWEEP_GRADIENT),
        }

        /**
         * Options for how the gradient is oriented relative to the drawable's bounds.
         * Used as value options for [ProfileTemplate.DesignTemplate.BackgroundTemplate.gradientOrientation].
         */
        enum class GradientOrientation(val orientation: GradientDrawable.Orientation) {
            /** Draw the gradient from the top to the bottom */
            TOP_BOTTOM(GradientDrawable.Orientation.TOP_BOTTOM),
            /** Draw the gradient from the right to the left */
            RIGHT_LEFT(GradientDrawable.Orientation.RIGHT_LEFT),
            /** Draw the gradient from the bottom to the top */
            BOTTOM_TOP(GradientDrawable.Orientation.BOTTOM_TOP),
            /** Draw the gradient from the left to the right */
            LEFT_RIGHT(GradientDrawable.Orientation.LEFT_RIGHT),
            /** Draw the gradient from the top-right to the bottom-left */
            TOPRIGHT_BOTTOMLEFT(GradientDrawable.Orientation.TR_BL),
            /** Draw the gradient from the bottom-right to the top-left */
            BOTTOMRIGHT_TOPLEFT(GradientDrawable.Orientation.BR_TL),
            /** Draw the gradient from the bottom-left to the top-right */
            BOTTOMLEFT_TOPRIGHT(GradientDrawable.Orientation.BL_TR),
            /** Draw the gradient from the top-left to the bottom-right */
            TOPLEFT_BOTTOMRIGHT(GradientDrawable.Orientation.TL_BR),
        }

        /**
         * Options for when and what sound should play (if ever).
         * Used as value options for [ProfileTemplate.SoundTemplate.trigger].
         */
        enum class TriggerSound {
            /** Sound off */
            NEVER,
            /** Play device sound only when a Whisper becomes visible when there are no other Whispers currently visible. */
            DEVICE_SOLE_WHISPER,
            /** Play device sound everytime a Whisper is created. */
            DEVICE_EVERY_WHISPER,
            /** Play custom sound only when a Whisper becomes visible when there are no other Whispers currently visible. */
            CUSTOM_SOLE_WHISPER,
            /** Play custom sound everytime a Whisper is created. */
            CUSTOM_EVERY_WHISPER,
        }

        /**
         * Options for when to vibrate (if ever).
         * Used as value options for [ProfileTemplate.VibrateTemplate.trigger].
         */
        enum class TriggerVibrate {
            /** Vibration off. */
            NEVER,
            /** Vibrate only when a Whisper becomes visible when there are no other Whispers currently visible. */
            SOLE_WHISPER,
            /** Vibrate everytime a Whisper is created. */
            EVERY_WHISPER,
        }

        /** Whisper profile options. Only used internally to determine what profile function was called. */
        internal enum class Profile {
            DEFAULT,
            ERROR,
            INFO,
            WARN,
            FATAL,
            CRITICAL,
            TRACE,
            DEBUG,
        }
    }

    /**
     * Creates a new Whisper using the Default profile.
     * Whisper.default() can also be used to create a Default Whisper instead of Whisper.default().
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    operator fun invoke(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = default(activity, message, duration, onClick)

    /**
     * Creates a new Whisper using the Default profile.
     * Whisper() can also be used to create a Default Whisper instead of Whisper.default().
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun default(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.DEFAULT)

    /**
     * Creates a new Whisper using the Info profile.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun info(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.INFO)

    /**
     * Creates a new Whisper using the Warn profile.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun warn(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.WARN)

    /**
     * Creates a new Whisper using the Error profile.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used.
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter].
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID.
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false,
     *   The Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun error(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.ERROR)

    /**
     * Creates a new Whisper using the Fatal profile.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun fatal(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.FATAL)

    /**
     * Creates a new Whisper using the Critical profile.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun critical(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.CRITICAL)

    /**
     * Creates a new Whisper using the Trace profile.
     * [Whisper.debug] and [Whisper.trace] will only be created if the app running in a debug build.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun trace(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.TRACE)

    /**
     * Creates a new Whisper using the Debug profile.
     * [Whisper.debug] and [Whisper.trace] will only be created if the app running in a debug build.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    fun debug(
        activity: Activity,
        message: String,
        duration: Long? = null,
        onClick: ((Activity, String) -> Unit)? = null,
    ) = build(activity, message, duration, onClick, Enums.Profile.DEBUG)

    /**
     * Called by all public Whisper build functions ([Whisper.default], [Whisper.info], [Whisper.warn], [Whisper.error], [Whisper.fatal], [Whisper.critical], [Whisper.trace], [Whisper.debug]) to create the new Whisper.
     *
     * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
     * @param message text to display within the Whisper.
     * @param duration OPTIONAL, how long the Whisper displays for before being auto-removed.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used. 
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter]. 
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param onClick OPTIONAL, will execute the passed block of code when tapped (if defined).
     *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [GlobalConfig.tapToDismiss] is true).
     *   Available withing [onClick] will be the Activity and its unique Whisper ID. 
     *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [GlobalConfig.tapToDismiss] is set to false, 
     *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
     * @param profile what public function was called.
     * @return a unique Whisper ID that can be ignored or passed to [Whisper.remove] to immediately remove.
     */
    private fun build(
        activity: Activity,
        message: String,
        duration: Long?,
        onClick: ((Activity, String) -> Unit)?,
        profile: Enums.Profile,
    ): String {
        // Do not proceed if message is empty or if using profiles DEBUG or TRACE while not running a debug build.
        if (activity.isProperBuildWithProfile(profile) && message.isNotEmpty()) {
            // Generate a unique id to associate with the Whisper so it can be removed in code if ever needed calling [Whisper.remove]
            val whisperId: String = UUID.randomUUID().toString()

            /*
            [runOnUiThread] here to avoid an "unable to instantiate appComponentFactory": java.lang.ClassNotFoundException: Didn't find class "androidx.core.app.CoreComponentFactory".
            This can happen when calling Whisper within coroutines not using [Dispatchers.Main] when previously calling Whisper in other activities.
             */
            activity.runOnUiThread {
                try {
                    BuildNewWhisper(
                        activity = activity,
                        whisperId = whisperId,
                        message = message,
                        profile = getProfileTemplate(profile),
                        duration = getStandardizedDuration(duration, message),
                        onClick = onClick,
                    )
                } catch (_: Exception) { /* do nothing */ }
            }
            return whisperId
        }
        return DEFAULT_WHISPER_ID
    }

    /**
     * [Whisper.finish] should be added to the onDestroy function of every activity that may use Whisper.
     * Failure to do so may cause a "android.view.WindowLeaked" error to occur when the activity is destroyed (if that activity created any Whispers).
     */
    fun finish(activity: Activity) = WhisperWindow.finish(activity)

    /**
     * Remove a single active Whisper immediately (includes invisible Whispers queued to show).
     *
     * @param activity current Activity.
     * @param whisperId The active Whisper to remove immediately. This unique value is returned after creating a Whisper with
     *   [Whisper.default], [Whisper.info], [Whisper.warn], [Whisper.error], [Whisper.fatal], [Whisper.critical], [Whisper.trace], [Whisper.debug].
     */
    fun remove(activity: Activity, whisperId: String) {
        if(whisperId.isNotBlank() && whisperId != DEFAULT_WHISPER_ID) {
            Util.traverseWhispers { order, viewWhisperId, view ->
                if (whisperId == viewWhisperId) {
                    view.removeWhisper(
                        activity = activity,
                        cancelTimer = Util.isWhisperUsingTimeout(
                            duration = view.extractDurationFromTag(0),
                            order = order
                        )
                    )
                }
            }
        }
    }

    /**
     * Clear all active Whispers (includes invisible Whispers queued to show).
     */
    fun clear(activity: Activity) {
        Util.traverseWhispers { _, _, view ->
            view.removeWhisper(activity, true)
        }
    }

    /**
     * Returns the current count of active Whispers (includes invisible Whispers queued to show).
     */
    fun activeWhisperCount(): Int {
        var count = 0
        Util.traverseWhispers { runningCount, _, _ ->
            count = runningCount
        }
        return count
    }

    /**
     * Takes the requested profile and returns the defined profile values to use when creating the Whisper.
     */
    private fun getProfileTemplate(profile: Enums.Profile): ProfileTemplate =
        when(profile) {
            Enums.Profile.DEFAULT -> Profiles.default
            Enums.Profile.ERROR -> Profiles.error
            Enums.Profile.INFO -> Profiles.info
            Enums.Profile.WARN -> Profiles.warn
            Enums.Profile.FATAL -> Profiles.fatal
            Enums.Profile.CRITICAL -> Profiles.critical
            Enums.Profile.TRACE -> Profiles.trace
            Enums.Profile.DEBUG -> Profiles.debug
        }

    /**
     * Ensures timeout length (if there should be a timeout at all) is within proper range 
     * (between [GlobalConfig.durationDisplayMinimum] and [GlobalConfig.durationDisplayMaximum]).
     *
     * @param duration OPTIONAL, a duration can be passed in to define the Whisper's auto-removing timeout.
     *   If this [duration] parameter is undefined, an auto-removing timeout will still be used.
     *   The timeout will be defined by the combination of the [message] length and [GlobalConfig.timeoutLengthPerCharacter].
     *   To have the Whisper never auto-timeout, pass 0 to [duration].
     * @param message The Whisper's message.
     * @return the duration in milliseconds to use before auto-removing the Whisper being created.
     */
    private fun getStandardizedDuration(duration: Long?, message: String): Long {
        // Get the duration either from the passed [duration] value or by detecting how long the timeout should be from the [message] length.
        val parsedDuration: Long = duration ?: run {

            // If [message] is empty, use the [GlobalConfig.durationDisplayMinimum] value to avoid the duration being 0 (no timeout).
            message.ifEmpty {
                return@run GlobalConfig.durationDisplayMinimum
            }

            // If [duration] was not passed in, take the [message] character length and multiply it by the [GlobalConfig.timeoutLengthPerCharacter].
            return@run GlobalConfig.timeoutLengthPerCharacter * message.count()
        }

        return when {
            // Timeout will not apply to Whisper with a duration of 0 or less.
            parsedDuration <= 0.0 -> 0
            // If timeout is less detected is less than the set minimum. Timeout will be the minimum.
            parsedDuration < GlobalConfig.durationDisplayMinimum -> GlobalConfig.durationDisplayMinimum
            // If [duration] was not passed in and timeout is greater than the set maximum, use the set max.
            // If [duration] was passed in and greater than the max, use the passed [duration].
            parsedDuration > GlobalConfig.durationDisplayMaximum && duration == null -> GlobalConfig.durationDisplayMaximum
            // Timeout is within range and does not need altering. Use exact value of parsedDuration.
            else -> parsedDuration
        }
    }
}