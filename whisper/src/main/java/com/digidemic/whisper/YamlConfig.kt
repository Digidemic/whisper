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

import android.app.Activity
import com.digidemic.kyaml.Kyaml
import com.digidemic.whisper.Constants.ANIMATION_TRANSITION_DURATION
import com.digidemic.whisper.Constants.CRITICAL_PROFILE
import com.digidemic.whisper.Constants.DEBUG_PROFILE
import com.digidemic.whisper.Constants.DEFAULT_PROFILE
import com.digidemic.whisper.Constants.DESIGN_BACKGROUND_COLORS
import com.digidemic.whisper.Constants.DESIGN_BACKGROUND_GRADIENT_CENTER_X
import com.digidemic.whisper.Constants.DESIGN_BACKGROUND_GRADIENT_CENTER_Y
import com.digidemic.whisper.Constants.DESIGN_BACKGROUND_GRADIENT_RADIUS
import com.digidemic.whisper.Constants.DESIGN_BACKGROUND_GRADIENT_ORIENTATION
import com.digidemic.whisper.Constants.DESIGN_BACKGROUND_TYPE
import com.digidemic.whisper.Constants.DESIGN_BORDER_CORNER_RADIUS_BOTTOM_LEFT
import com.digidemic.whisper.Constants.DESIGN_BORDER_CORNER_RADIUS_BOTTOM_RIGHT
import com.digidemic.whisper.Constants.DESIGN_BORDER_CORNER_RADIUS_TOP_LEFT
import com.digidemic.whisper.Constants.DESIGN_BORDER_CORNER_RADIUS_TOP_RIGHT
import com.digidemic.whisper.Constants.DESIGN_BORDER_COLOR
import com.digidemic.whisper.Constants.DESIGN_BORDER_SIZE
import com.digidemic.whisper.Constants.DISPLAY_SPACE
import com.digidemic.whisper.Constants.DESIGN_PADDING_BOTTOM
import com.digidemic.whisper.Constants.DESIGN_PADDING_LEFT
import com.digidemic.whisper.Constants.DESIGN_PADDING_RIGHT
import com.digidemic.whisper.Constants.DESIGN_PADDING_TOP
import com.digidemic.whisper.Constants.DESIGN_SHADOW_CAST_SHADOW
import com.digidemic.whisper.Constants.DESIGN_SHADOW_COLOR
import com.digidemic.whisper.Constants.DESIGN_SHADOW_CORNER_RADIUS_BOTTOM_LEFT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_CORNER_RADIUS_BOTTOM_RIGHT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_CORNER_RADIUS_TOP_LEFT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_CORNER_RADIUS_TOP_RIGHT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_INSET_BOTTOM
import com.digidemic.whisper.Constants.DESIGN_SHADOW_INSET_LEFT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_INSET_RIGHT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_INSET_TOP
import com.digidemic.whisper.Constants.DESIGN_SHADOW_PADDING_BOTTOM
import com.digidemic.whisper.Constants.DESIGN_SHADOW_PADDING_LEFT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_PADDING_RIGHT
import com.digidemic.whisper.Constants.DESIGN_SHADOW_PADDING_TOP
import com.digidemic.whisper.Constants.DESIGN_TEXT_COLOR
import com.digidemic.whisper.Constants.DESIGN_TEXT_FONT_BOLD
import com.digidemic.whisper.Constants.DESIGN_TEXT_FONT_FONT_FAMILY
import com.digidemic.whisper.Constants.DESIGN_TEXT_FONT_ITALIC
import com.digidemic.whisper.Constants.DESIGN_TEXT_FONT_UNDERLINE
import com.digidemic.whisper.Constants.DESIGN_TEXT_GRAVITY
import com.digidemic.whisper.Constants.DESIGN_TEXT_SIZE
import com.digidemic.whisper.Constants.DURATION_DISPLAY_MAXIMUM
import com.digidemic.whisper.Constants.DURATION_DISPLAY_MINIMUM
import com.digidemic.whisper.Constants.TIMEOUT_LENGTH_PER_CHARACTER
import com.digidemic.whisper.Constants.ERROR_PROFILE
import com.digidemic.whisper.Constants.FATAL_PROFILE
import com.digidemic.whisper.Constants.INFO_PROFILE
import com.digidemic.whisper.Constants.YAML_KEY_DELIMITER
import com.digidemic.whisper.Constants.MAX_VISIBLE
import com.digidemic.whisper.Constants.OFFSET_ADDITIONAL_OFFSET_FOR_STATUS_BAR
import com.digidemic.whisper.Constants.OFFSET_X
import com.digidemic.whisper.Constants.OFFSET_Y
import com.digidemic.whisper.Constants.PIXEL_DENSITY_UNIT
import com.digidemic.whisper.Constants.TIMEOUT_ONLY_FOR_OLDEST_WHISPER
import com.digidemic.whisper.Constants.SORT_ORDER
import com.digidemic.whisper.Constants.SOUND_CUSTOM_SOUND
import com.digidemic.whisper.Constants.SOUND_TRIGGER
import com.digidemic.whisper.Constants.POSITION_ON_SCREEN
import com.digidemic.whisper.Constants.TAP_TO_DISMISS
import com.digidemic.whisper.Constants.TRACE_PROFILE
import com.digidemic.whisper.Constants.VIBRATE_TRIGGER
import com.digidemic.whisper.Constants.VIBRATE_VIBRATION_PATTERN
import com.digidemic.whisper.Constants.WARN_PROFILE
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Called the first time Whisper is used to potentially update the default settings with a provided YAML file.
 * If whisper.yaml is found in the module's assets folder (Example: YourProject\app\src\main\assets\whisper.yaml)
 * the set values in the YAML update Whisper's [Whisper.GlobalConfig] and [ProfileTemplate] configurations.
 */
internal object YamlConfig {

    /** Flag set true if whisper.yaml has attempted to be applied. */
    private val kyamlRan = AtomicBoolean(false)

    /**
     * Parse and apply assets/whisper.yaml [Whisper.GlobalConfig] and [ProfileTemplate]
     * only if it has not already been applied (see [kyamlRan]).
     */
    fun applyIfNeverRan(activity: Activity){
        if(!kyamlRan.get()) {
            apply(activity)
        }
    }

    /** Parse and apply assets/whisper.yaml [Whisper.GlobalConfig] and [ProfileTemplate]. */
    fun apply(activity: Activity) {
        kyamlRan.set(true)
        Kyaml(
            activity = activity,
            yamlFileNameInAssets = Whisper.GlobalConfig.yamlFileName,
            onEachItem = { key, value ->
                try {
                    with(Whisper.GlobalConfig) {
                        val str = value.toString()
                        when (key) {
                            PIXEL_DENSITY_UNIT -> pixelDensityUnit = Whisper.Enums.PixelDensity.valueOf(str.uppercase())
                            POSITION_ON_SCREEN -> positionOnScreen = Whisper.Enums.PositionOnScreen.valueOf(str.uppercase())
                            SORT_ORDER -> sortOrder = Whisper.Enums.SortOrder.valueOf(str.uppercase())
                            MAX_VISIBLE -> maxVisible = str.toInt()
                            DISPLAY_SPACE -> displaySpace = str.toInt()
                            TIMEOUT_LENGTH_PER_CHARACTER -> timeoutLengthPerCharacter = str.toLong()
                            DURATION_DISPLAY_MINIMUM -> durationDisplayMinimum = str.toLong()
                            DURATION_DISPLAY_MAXIMUM -> durationDisplayMaximum = str.toLong()
                            ANIMATION_TRANSITION_DURATION -> animationTransitionDuration = str.toLong()
                            TIMEOUT_ONLY_FOR_OLDEST_WHISPER -> timeoutOnlyForOldestWhisper = str.toBoolean()
                            TAP_TO_DISMISS -> tapToDismiss = str.toBoolean()
                            OFFSET_X -> offset.x = str.toInt()
                            OFFSET_Y -> offset.y = str.toInt()
                            OFFSET_ADDITIONAL_OFFSET_FOR_STATUS_BAR -> offset.additionalOffsetForStatusBar = str.toBoolean()
                            else -> {
                                key.split(YAML_KEY_DELIMITER).let { keySplit ->
                                    if(keySplit.size >= 2) {
                                        val profileType = keySplit.firstOrNull()
                                        val objectKey = keySplit.drop(1).joinToString(YAML_KEY_DELIMITER)

                                        applyToProfile(
                                            key = objectKey,
                                            value = value,
                                            str = str,
                                            profile = when(profileType) {
                                                DEFAULT_PROFILE -> Whisper.Profiles.default
                                                ERROR_PROFILE -> Whisper.Profiles.error
                                                INFO_PROFILE -> Whisper.Profiles.info
                                                WARN_PROFILE -> Whisper.Profiles.warn
                                                FATAL_PROFILE -> Whisper.Profiles.fatal
                                                CRITICAL_PROFILE -> Whisper.Profiles.critical
                                                TRACE_PROFILE -> Whisper.Profiles.trace
                                                DEBUG_PROFILE -> Whisper.Profiles.debug
                                                else -> null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Invalid value, default value used for item.
                }
            },
        )
    }

    /**
     * Update a single specific profile's configuration variable.
     */
    private fun applyToProfile(key: String, value: Any?, str: String, profile: ProfileTemplate?) {
        if(profile == null) return
        with(profile.design) {
            when(key) {
                SOUND_TRIGGER -> profile.sound.trigger = Whisper.Enums.TriggerSound.valueOf(str.uppercase())
                SOUND_CUSTOM_SOUND -> profile.sound.customSound = value as? String
                VIBRATE_TRIGGER -> profile.vibrate.trigger = Whisper.Enums.TriggerVibrate.valueOf(str.uppercase())
                VIBRATE_VIBRATION_PATTERN -> profile.vibrate.vibrationPattern = (value as List<*>).map { it.toString().toLong() }.toLongArray()
                DESIGN_PADDING_LEFT -> padding.left = str.toInt()
                DESIGN_PADDING_TOP -> padding.top = str.toInt()
                DESIGN_PADDING_RIGHT -> padding.right = str.toInt()
                DESIGN_PADDING_BOTTOM -> padding.bottom = str.toInt()
                DESIGN_BORDER_CORNER_RADIUS_TOP_LEFT -> border.cornerRadius.topLeft = str.toFloat()
                DESIGN_BORDER_CORNER_RADIUS_TOP_RIGHT -> border.cornerRadius.topRight = str.toFloat()
                DESIGN_BORDER_CORNER_RADIUS_BOTTOM_RIGHT -> border.cornerRadius.bottomRight = str.toFloat()
                DESIGN_BORDER_CORNER_RADIUS_BOTTOM_LEFT -> border.cornerRadius.bottomLeft = str.toFloat()
                DESIGN_BORDER_COLOR -> border.color = str
                DESIGN_BORDER_SIZE -> border.size = str.toInt()
                DESIGN_BACKGROUND_TYPE -> background.type = Whisper.Enums.BackgroundType.valueOf(str.uppercase())
                DESIGN_BACKGROUND_COLORS -> background.colors = (value as List<*>).map { it.toString() }
                DESIGN_BACKGROUND_GRADIENT_CENTER_X -> background.gradientCenterX = str.toFloat()
                DESIGN_BACKGROUND_GRADIENT_CENTER_Y -> background.gradientCenterY = str.toFloat()
                DESIGN_BACKGROUND_GRADIENT_RADIUS -> background.gradientRadius = str.toFloat()
                DESIGN_BACKGROUND_GRADIENT_ORIENTATION -> background.gradientOrientation = Whisper.Enums.GradientOrientation.valueOf(str.uppercase())
                DESIGN_TEXT_COLOR -> text.color = str
                DESIGN_TEXT_SIZE -> text.size = str.toFloat()
                DESIGN_TEXT_FONT_BOLD -> text.font.bold = str.toBoolean()
                DESIGN_TEXT_FONT_ITALIC -> text.font.italic = str.toBoolean()
                DESIGN_TEXT_FONT_UNDERLINE -> text.font.underline = str.toBoolean()
                DESIGN_TEXT_FONT_FONT_FAMILY -> text.font.fontFamily = value as? String
                DESIGN_TEXT_GRAVITY -> text.gravity = Whisper.Enums.TextGravity.valueOf(str.uppercase())
                DESIGN_SHADOW_CAST_SHADOW -> shadow.castShadow = str.toBoolean()
                DESIGN_SHADOW_COLOR -> shadow.color = str
                DESIGN_SHADOW_CORNER_RADIUS_TOP_LEFT -> shadow.cornerRadius.topLeft = str.toFloat()
                DESIGN_SHADOW_CORNER_RADIUS_TOP_RIGHT -> shadow.cornerRadius.topRight = str.toFloat()
                DESIGN_SHADOW_CORNER_RADIUS_BOTTOM_RIGHT -> shadow.cornerRadius.bottomRight = str.toFloat()
                DESIGN_SHADOW_CORNER_RADIUS_BOTTOM_LEFT -> shadow.cornerRadius.bottomLeft = str.toFloat()
                DESIGN_SHADOW_INSET_LEFT -> shadow.inset.left = str.toInt()
                DESIGN_SHADOW_INSET_TOP -> shadow.inset.top = str.toInt()
                DESIGN_SHADOW_INSET_RIGHT -> shadow.inset.right = str.toInt()
                DESIGN_SHADOW_INSET_BOTTOM -> shadow.inset.bottom = str.toInt()
                DESIGN_SHADOW_PADDING_LEFT -> shadow.padding.left = str.toInt()
                DESIGN_SHADOW_PADDING_TOP -> shadow.padding.top = str.toInt()
                DESIGN_SHADOW_PADDING_RIGHT -> shadow.padding.right = str.toInt()
                DESIGN_SHADOW_PADDING_BOTTOM -> shadow.padding.bottom = str.toInt()
            }
        }
    }
}