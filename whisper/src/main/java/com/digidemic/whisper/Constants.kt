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

import android.graphics.Color
import androidx.annotation.ColorInt

internal object Constants {

    /** View tag name for individual Whisper. */
    const val WHISPER_VIEW_NAME_TAG = "WhisperView"

    /** Delimiter within element tags separating the tag name, Whisper id, and the duration value. */
    const val WHISPER_VIEW_TAG_DELIMITER = "|"

    /** Directory /raw/ used to store custom sound files. */
    const val RAW_DIRECTORY_NAME = "raw"

    /** YAML file name expected in /assets/ directory to be used for loading in all custom configurations. */
    const val DEFAULT_YAML_FILE_NAME = "whisper.yaml"

    /** Creating a Whisper returns a unique Whisper ID. When a Whisper cannot be created, the default value is returned. */
    const val DEFAULT_WHISPER_ID = "-1"

    /** Default color when parsing a hex color code (String) into a color int fails. */
    @ColorInt
    const val DEFAULT_PARSING_FAILING_COLOR = Color.DKGRAY

    /**
     * whisper.yaml is parsed using [com.digidemic.kyaml.Kyaml] where each item's key/value pair is returned.
     * Nested keys are returned with each parent period delimited.
     * When parsing the key in [YamlConfig], the the following is used to split the key.
     *
     * Example: "Design.Border.color"
     */
    const val YAML_KEY_DELIMITER = "."

    /** Profile root name in whisper.yaml. */
    const val DEFAULT_PROFILE = "DefaultProfile"
    const val ERROR_PROFILE = "ErrorProfile"
    const val INFO_PROFILE = "InfoProfile"
    const val WARN_PROFILE = "WarnProfile"
    const val FATAL_PROFILE = "FatalProfile"
    const val CRITICAL_PROFILE = "CriticalProfile"
    const val TRACE_PROFILE = "TraceProfile"
    const val DEBUG_PROFILE = "DebugProfile"

    /** All whisper.yaml item keys with the associated parents. */
    const val PIXEL_DENSITY_UNIT = "pixelDensityUnit"
    const val POSITION_ON_SCREEN = "positionOnScreen"
    const val SORT_ORDER = "sortOrder"
    const val MAX_VISIBLE = "maxVisible"
    const val DISPLAY_SPACE = "displaySpace"
    const val TIMEOUT_LENGTH_PER_CHARACTER = "timeoutLengthPerCharacter"
    const val DURATION_DISPLAY_MINIMUM = "durationDisplayMinimum"
    const val DURATION_DISPLAY_MAXIMUM = "durationDisplayMaximum"
    const val ANIMATION_TRANSITION_DURATION = "animationTransitionDuration"
    const val TIMEOUT_ONLY_FOR_OLDEST_WHISPER = "timeoutOnlyForOldestWhisper"
    const val TAP_TO_DISMISS = "tapToDismiss"
    const val OFFSET_X = "Offset.x"
    const val OFFSET_Y = "Offset.y"
    const val OFFSET_ADDITIONAL_OFFSET_FOR_STATUS_BAR = "Offset.additionalOffsetForStatusBar"
    const val SOUND_CUSTOM_SOUND = "Sound.customSound"
    const val SOUND_TRIGGER = "Sound.trigger"
    const val VIBRATE_VIBRATION_PATTERN = "Vibrate.vibrationPattern"
    const val VIBRATE_TRIGGER = "Vibrate.trigger"
    const val DESIGN_PADDING_LEFT = "Design.Padding.left"
    const val DESIGN_PADDING_TOP = "Design.Padding.top"
    const val DESIGN_PADDING_RIGHT = "Design.Padding.right"
    const val DESIGN_PADDING_BOTTOM = "Design.Padding.bottom"
    const val DESIGN_BORDER_CORNER_RADIUS_TOP_LEFT = "Design.Border.CornerRadius.topLeft"
    const val DESIGN_BORDER_CORNER_RADIUS_TOP_RIGHT = "Design.Border.CornerRadius.topRight"
    const val DESIGN_BORDER_CORNER_RADIUS_BOTTOM_RIGHT = "Design.Border.CornerRadius.bottomRight"
    const val DESIGN_BORDER_CORNER_RADIUS_BOTTOM_LEFT = "Design.Border.CornerRadius.bottomLeft"
    const val DESIGN_BORDER_COLOR = "Design.Border.color"
    const val DESIGN_BORDER_SIZE = "Design.Border.size"
    const val DESIGN_BACKGROUND_TYPE = "Design.Background.type"
    const val DESIGN_BACKGROUND_COLORS = "Design.Background.colors"
    const val DESIGN_BACKGROUND_GRADIENT_CENTER_X = "Design.Background.gradientCenterX"
    const val DESIGN_BACKGROUND_GRADIENT_CENTER_Y = "Design.Background.gradientCenterY"
    const val DESIGN_BACKGROUND_GRADIENT_RADIUS = "Design.Background.gradientRadius"
    const val DESIGN_BACKGROUND_GRADIENT_ORIENTATION = "Design.Background.gradientOrientation"
    const val DESIGN_TEXT_COLOR = "Design.Text.color"
    const val DESIGN_TEXT_SIZE = "Design.Text.size"
    const val DESIGN_TEXT_FONT_BOLD = "Design.Text.Font.bold"
    const val DESIGN_TEXT_FONT_ITALIC = "Design.Text.Font.italic"
    const val DESIGN_TEXT_FONT_UNDERLINE = "Design.Text.Font.underline"
    const val DESIGN_TEXT_FONT_FONT_FAMILY = "Design.Text.Font.fontFamily"
    const val DESIGN_TEXT_GRAVITY = "Design.Text.gravity"
    const val DESIGN_SHADOW_CAST_SHADOW = "Design.Shadow.castShadow"
    const val DESIGN_SHADOW_COLOR = "Design.Shadow.color"
    const val DESIGN_SHADOW_CORNER_RADIUS_TOP_LEFT = "Design.Shadow.CornerRadius.topLeft"
    const val DESIGN_SHADOW_CORNER_RADIUS_TOP_RIGHT = "Design.Shadow.CornerRadius.topRight"
    const val DESIGN_SHADOW_CORNER_RADIUS_BOTTOM_RIGHT = "Design.Shadow.CornerRadius.bottomRight"
    const val DESIGN_SHADOW_CORNER_RADIUS_BOTTOM_LEFT = "Design.Shadow.CornerRadius.bottomLeft"
    const val DESIGN_SHADOW_INSET_LEFT = "Design.Shadow.Inset.left"
    const val DESIGN_SHADOW_INSET_TOP = "Design.Shadow.Inset.top"
    const val DESIGN_SHADOW_INSET_RIGHT = "Design.Shadow.Inset.right"
    const val DESIGN_SHADOW_INSET_BOTTOM = "Design.Shadow.Inset.bottom"
    const val DESIGN_SHADOW_PADDING_LEFT = "Design.Shadow.Padding.left"
    const val DESIGN_SHADOW_PADDING_TOP = "Design.Shadow.Padding.top"
    const val DESIGN_SHADOW_PADDING_RIGHT = "Design.Shadow.Padding.right"
    const val DESIGN_SHADOW_PADDING_BOTTOM = "Design.Shadow.Padding.bottom"

    /** Default profile background colors. */
    const val DEFAULT_DEFAULT_PROFILE_BACKGROUND_COLOR = "BB66C2A5"
    const val DEFAULT_INFO_PROFILE_BACKGROUND_COLOR = "BB3288BD"
    const val DEFAULT_WARN_PROFILE_BACKGROUND_COLOR = "BBF46D43"
    const val DEFAULT_ERROR_PROFILE_BACKGROUND_COLOR = "BBD53E4F"
    const val DEFAULT_FATAL_PROFILE_BACKGROUND_COLOR = "BB9E0142"
    const val DEFAULT_CRITICAL_PROFILE_BACKGROUND_COLOR = "BB5E4FA2"
    const val DEFAULT_TRACE_PROFILE_BACKGROUND_COLOR = "FDAE61"
    const val DEFAULT_DEBUG_PROFILE_BACKGROUND_COLOR = "ABDDA4"

    /** Default profile border colors. */
    const val DEFAULT_DEFAULT_PROFILE_BORDER_COLOR = "CAF2E5"
    const val DEFAULT_INFO_PROFILE_BORDER_COLOR = "7FBBDF"
    const val DEFAULT_WARN_PROFILE_BORDER_COLOR = "FFAD93"
    const val DEFAULT_ERROR_PROFILE_BORDER_COLOR = "F8919D"
    const val DEFAULT_FATAL_PROFILE_BORDER_COLOR = "CF3978"
    const val DEFAULT_CRITICAL_PROFILE_BORDER_COLOR = "B0A6DB"
    const val DEFAULT_TRACE_PROFILE_BORDER_COLOR = "BE6B1A"
    const val DEFAULT_DEBUG_PROFILE_BORDER_COLOR = "51A044"

    /** Default [Whisper.GlobalConfig] and [Whisper.Profiles] values. */
    val defaultPixelDensityUnit = Whisper.Enums.PixelDensity.DP
    val defaultPositionOnScreen = Whisper.Enums.PositionOnScreen.TOP_LEFT
    val defaultSortOrder = Whisper.Enums.SortOrder.BELOW
    const val DEFAULT_MAX_VISIBLE = 3
    const val DEFAULT_DISPLAY_SPACE = 12
    const val DEFAULT_TIMEOUT_LENGTH_PER_CHARACTER = 75L
    const val DEFAULT_DURATION_DISPLAY_MINIMUM = 2200L
    const val DEFAULT_DURATION_DISPLAY_MAXIMUM = 22000L
    const val DEFAULT_ANIMATION_TRANSITION_DURATION = 400L
    const val DEFAULT_TIMEOUT_ONLY_FOR_OLDEST_WHISPER = true
    const val DEFAULT_TAP_TO_DISMISS = true
    const val DEFAULT_OFFSET_X = 12
    const val DEFAULT_OFFSET_Y = 12
    const val DEFAULT_ADDITIONAL_OFFSET_FOR_STATUS_BAR = true
    val defaultSoundTrigger = Whisper.Enums.TriggerSound.NEVER
    val defaultCustomSound: String? = null
    val defaultVibrateTrigger = Whisper.Enums.TriggerVibrate.NEVER
    val defaultVibrationPattern = longArrayOf(0, 100, 50, 100)
    const val DEFAULT_DESIGN_PADDING = 8
    const val DEFAULT_TEXT_COLOR = "FEFEFE"
    const val DEFAULT_TEXT_SIZE = 20f
    val defaultTextGravity = Whisper.Enums.TextGravity.LEFT
    const val DEFAULT_FONT_BOLD = false
    const val DEFAULT_FONT_ITALIC = false
    const val DEFAULT_FONT_UNDERLINE = false
    val DEFAULT_FONT_FAMILY: String? = null
    val DEFAULT_BACKGROUND_TYPE = Whisper.Enums.BackgroundType.SOLID
    const val DEFAULT_BACKGROUND_COLOR = "BB020202"
    val defaultGradientOrientation = Whisper.Enums.GradientOrientation.TOP_BOTTOM
    const val DEFAULT_GRADIENT_CENTER_X = 0.5f
    const val DEFAULT_GRADIENT_CENTER_Y = 0.5f
    const val DEFAULT_GRADIENT_RADIUS = 120f
    const val DEFAULT_CORNER_RADIUS = 8f
    const val DEFAULT_BORDER_STOKE_WIDTH = 4
    const val DEFAULT_BORDER_STROKE_COLOR = "CAF2E5"
    const val DEFAULT_CAST_SHADOW = true
    const val DEFAULT_SHADOW_COLOR = "88676767"
    const val DEFAULT_SHADOW_INSET_LEFT = 8
    const val DEFAULT_SHADOW_INSET_TOP = 8
    const val DEFAULT_SHADOW_INSET_RIGHT = 0
    const val DEFAULT_SHADOW_INSET_BOTTOM = 0
    const val DEFAULT_SHADOW_PADDING_LEFT = 0
    const val DEFAULT_SHADOW_PADDING_TOP = 0
    const val DEFAULT_SHADOW_PADDING_RIGHT = 8
    const val DEFAULT_SHADOW_PADDING_BOTTOM = 8
    const val DEFAULT_PADDING_TEMPLATE = 0
    const val DEFAULT_WHISPER_MARGIN = 0
}