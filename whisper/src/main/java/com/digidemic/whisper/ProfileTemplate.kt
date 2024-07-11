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

import android.graphics.drawable.GradientDrawable
import com.digidemic.whisper.extensions.ColorExtensions.toColorInt
import com.digidemic.whisper.extensions.ColorExtensions.toColorIntArray
import com.digidemic.whisper.Constants.DEFAULT_BACKGROUND_TYPE
import com.digidemic.whisper.Constants.DEFAULT_CAST_SHADOW
import com.digidemic.whisper.Constants.DEFAULT_FONT_BOLD
import com.digidemic.whisper.Constants.DEFAULT_FONT_FAMILY
import com.digidemic.whisper.Constants.DEFAULT_FONT_ITALIC
import com.digidemic.whisper.Constants.DEFAULT_FONT_UNDERLINE
import com.digidemic.whisper.Constants.defaultCustomSound
import com.digidemic.whisper.Constants.defaultGradientOrientation
import com.digidemic.whisper.Constants.defaultSoundTrigger
import com.digidemic.whisper.Constants.defaultTextGravity
import com.digidemic.whisper.Constants.defaultVibrateTrigger
import com.digidemic.whisper.extensions.greaterThanZero
import com.digidemic.whisper.extensions.zeroOrGreater

/**
 * ProfileTemplate is used to setup all profiles.
 * This class is used internally by Whisper meaning there is no reason to instantiated this class outside of Whisper's code.
 * To make changes to individual profile values, update the whisper.yaml config file (recommended). Or with code at runtime with [Whisper.Profiles] (not recommended).
 */
class ProfileTemplate(
    backgroundHexColor: String,
    borderHexColor: String,
) {
    /** Sound settings for the profile. */
    val sound: SoundTemplate = SoundTemplate()

    /** Vibrate settings for the profile. */
    val vibrate: VibrateTemplate = VibrateTemplate()

    /** Design settings for the profile. */
    val design: DesignTemplate = DesignTemplate()

    init {
        design.background.colors = listOf(backgroundHexColor)
        design.border.color = borderHexColor
    }

    class SoundTemplate {
        /**
         * When and what sound should play (if ever).
         * If setting [Whisper.Enums.TriggerSound.CUSTOM_SOLE_WHISPER] or [Whisper.Enums.TriggerSound.CUSTOM_EVERY_WHISPER],
         * see [ProfileTemplate.SoundTemplate.customSound] to use a custom sound.
         *
         * Options:
         *   - [Whisper.Enums.TriggerSound.NEVER]
         *   - [Whisper.Enums.TriggerSound.DEVICE_SOLE_WHISPER]
         *   - [Whisper.Enums.TriggerSound.DEVICE_EVERY_WHISPER]
         *   - [Whisper.Enums.TriggerSound.CUSTOM_SOLE_WHISPER]
         *   - [Whisper.Enums.TriggerSound.CUSTOM_EVERY_WHISPER]
         */
        var trigger: Whisper.Enums.TriggerSound = defaultSoundTrigger

        /**
         * Only used if [SoundTemplate.trigger] is [Whisper.Enums.TriggerSound.CUSTOM_SOLE_WHISPER]
         * or [Whisper.Enums.TriggerSound.CUSTOM_EVERY_WHISPER] to play the externally added sound.
         * Sound file must be in module's res/raw directory.
         * Value here should be the sound file name without the extension.
         *
         * Example: src/main/res/raw/horn.mp3 -> "horn"
         */
        var customSound: String? = defaultCustomSound
    }

    class VibrateTemplate {
        /**
         * When to vibrate (if ever).
         * If setting anything other than [Whisper.Enums.TriggerVibrate.NEVER], see [ProfileTemplate.VibrateTemplate.vibrationPattern] to define a custom vibration pattern.
         *
         * REQUIRED: The vibrate permission must be added in your module's manifest using Whisper for vibration to work. <uses-permission android:name="android.permission.VIBRATE"/>
         *
         * Options:
         *   - [Whisper.Enums.TriggerVibrate.NEVER]
         *   - [Whisper.Enums.TriggerVibrate.SOLE_WHISPER]
         *   - [Whisper.Enums.TriggerVibrate.EVERY_WHISPER]
         */
        var trigger: Whisper.Enums.TriggerVibrate = defaultVibrateTrigger

        /**
         * In milliseconds, vibration sequence of off/on/off/on... if [VibrateTemplate.trigger] is not [Whisper.Enums.TriggerVibrate.NEVER].
         * If first value is 0, there will be no initial vibration delay.
         * Second value vibrates for the duration defined.
         * Third will delay for the duration.
         * and so on for desired vibration effect.
         */
        var vibrationPattern: LongArray = Constants.defaultVibrationPattern
            get() = if(field.isNotEmpty()) field else Constants.defaultVibrationPattern
    }

    class DesignTemplate {
        /**
         * Padding between Whisper edges (left, top, right, bottom) and text.
         * This will make the overall Whisper size larger.
         */
        val padding: PaddingTemplate = PaddingTemplate(
            left = Constants.DEFAULT_DESIGN_PADDING,
            top = Constants.DEFAULT_DESIGN_PADDING,
            right = Constants.DEFAULT_DESIGN_PADDING,
            bottom = Constants.DEFAULT_DESIGN_PADDING,
        )

        /** Text and font settings for the profile. */
        val text: TextTemplate = TextTemplate()

        /** Background settings for the profile. */
        val background: BackgroundTemplate = BackgroundTemplate()

        /** Border (outline) settings for the profile. */
        val border: BorderTemplate = BorderTemplate()

        /** Shadow settings for the profile. */
        val shadow: ShadowTemplate = ShadowTemplate()

        class PaddingTemplate(
            left: Int,
            top: Int,
            right: Int,
            bottom: Int
        ) {
            /** Left padding, must be 0 or greater. Otherwise will default to [Constants.DEFAULT_PADDING_TEMPLATE]. */
            var left: Int = left
                get() = field.zeroOrGreater(Constants.DEFAULT_PADDING_TEMPLATE)

            /** Top padding, must be 0 or greater. Otherwise will default to [Constants.DEFAULT_PADDING_TEMPLATE]. */
            var top: Int = top
                get() = field.zeroOrGreater(Constants.DEFAULT_PADDING_TEMPLATE)

            /** Right padding, must be 0 or greater. Otherwise will default to [Constants.DEFAULT_PADDING_TEMPLATE]. */
            var right: Int = right
                get() = field.zeroOrGreater(Constants.DEFAULT_PADDING_TEMPLATE)

            /** Bottom padding, must be 0 or greater. Otherwise will default to [Constants.DEFAULT_PADDING_TEMPLATE]. */
            var bottom: Int = bottom
                get() = field.zeroOrGreater(Constants.DEFAULT_PADDING_TEMPLATE)
        }

        class CornerRadius {
            /** Top-Left corner radius (corner roundness), must be 0 or greater. Otherwise will default to [Constants.DEFAULT_CORNER_RADIUS]. */
            var topLeft: Float = Constants.DEFAULT_CORNER_RADIUS
                get() = field.zeroOrGreater(Constants.DEFAULT_CORNER_RADIUS)

            /** Top-Right corner radius (corner roundness), must be 0 or greater. Otherwise will default to [Constants.DEFAULT_CORNER_RADIUS]. */
            var topRight: Float = Constants.DEFAULT_CORNER_RADIUS
                get() = field.zeroOrGreater(Constants.DEFAULT_CORNER_RADIUS)

            /** Bottom-Right corner radius (corner roundness), must be 0 or greater. Otherwise will default to [Constants.DEFAULT_CORNER_RADIUS]. */
            var bottomRight: Float = Constants.DEFAULT_CORNER_RADIUS
                get() = field.zeroOrGreater(Constants.DEFAULT_CORNER_RADIUS)

            /** Bottom-Left corner radius (corner roundness), must be 0 or greater. Otherwise will default to [Constants.DEFAULT_CORNER_RADIUS]. */
            var bottomLeft: Float = Constants.DEFAULT_CORNER_RADIUS
                get() = field.zeroOrGreater(Constants.DEFAULT_CORNER_RADIUS)

            /** non-public field used by Whisper to arranges and return an array of all corner radius that is consumed by [GradientDrawable.setCornerRadii]. */
            internal val radii: FloatArray
                get() = floatArrayOf(
                    topLeft,
                    topLeft,
                    topRight,
                    topRight,
                    bottomRight,
                    bottomRight,
                    bottomLeft,
                    bottomLeft,
                )
        }

        class TextTemplate {
            /**
             * Whisper text color.
             * Value must be a 6 or 8 character color hex code as "RRGGBB" or "AARRGGBB".
             */
            var color: String = Constants.DEFAULT_TEXT_COLOR

            /** Whisper text size. */
            var size: Float = Constants.DEFAULT_TEXT_SIZE
                get() = field.greaterThanZero(Constants.DEFAULT_TEXT_SIZE)

            /**
             * Sets the horizontal alignment of a Whisper's text.
             *
             * Options:
             *   - [Whisper.Enums.TextGravity.LEFT]
             *   - [Whisper.Enums.TextGravity.RIGHT]
             *   - [Whisper.Enums.TextGravity.CENTER]
             *   - [Whisper.Enums.TextGravity.START]
             *   - [Whisper.Enums.TextGravity.END]
             */
            var gravity: Whisper.Enums.TextGravity = defaultTextGravity

            /** Font configurations including bolding, italicizing, underlining, and font family. */
            val font: FontTemplate = FontTemplate()

            /** non-public field used by Whisper to convert the user defined color hex code to a color int. */
            internal val colorInt: Int
                get() = color.toColorInt(Constants.DEFAULT_TEXT_COLOR)

            class FontTemplate {
                /** If text should be bolded. */
                var bold: Boolean = DEFAULT_FONT_BOLD

                /** If text should be italicized. */
                var italic: Boolean = DEFAULT_FONT_ITALIC

                /** If text should be underlined. */
                var underline: Boolean = DEFAULT_FONT_UNDERLINE

                /**
                 * .ttf or .otf fonts found in module's /assets/ can be used as the font family.
                 * Value here should be the font name with the extension (with the same casing as the file name).
                 *
                 * Example: src/main/assets/Miracode.ttf -> "Miracode.ttf"
                 */
                var fontFamily: String? = DEFAULT_FONT_FAMILY
            }
        }

        class BackgroundTemplate {
            /**
             * If background is a solid color or a type of gradient.
             * If [Whisper.Enums.BackgroundType.SOLID], [BackgroundTemplate.colors] will only use the first color in the list to fill the background.
             * All gradient specific configuration fields in [BackgroundTemplate] will also be ignored if [Whisper.Enums.BackgroundType.SOLID].
             *
             * Options:
             *   - [Whisper.Enums.BackgroundType.SOLID]
             *   - [Whisper.Enums.BackgroundType.GRADIENT_LINEAR]
             *   - [Whisper.Enums.BackgroundType.GRADIENT_RADIAL]
             *   - [Whisper.Enums.BackgroundType.GRADIENT_SWEEP]
             */
            var type: Whisper.Enums.BackgroundType = DEFAULT_BACKGROUND_TYPE

            /**
             * Background color(s).
             * If [BackgroundTemplate.type] is [Whisper.Enums.BackgroundType.SOLID], only the first color of list is used.
             * Otherwise multiple colors can be used in gradient background types.
             * Values must be a 6 or 8 character color hex code as "RRGGBB" or "AARRGGBB".
             */
            var colors: List<String> = listOf(Constants.DEFAULT_BACKGROUND_COLOR)

            /**
             * Sets the angle of the gradient.
             * Only used if [BackgroundTemplate.type] is not [Whisper.Enums.BackgroundType.SOLID].
             *
             * Options:
             *   - [Whisper.Enums.GradientOrientation.TOP_BOTTOM],
             *   - [Whisper.Enums.GradientOrientation.RIGHT_LEFT],
             *   - [Whisper.Enums.GradientOrientation.BOTTOM_TOP],
             *   - [Whisper.Enums.GradientOrientation.LEFT_RIGHT],
             *   - [Whisper.Enums.GradientOrientation.TOPRIGHT_BOTTOMLEFT],
             *   - [Whisper.Enums.GradientOrientation.BOTTOMRIGHT_TOPLEFT],
             *   - [Whisper.Enums.GradientOrientation.BOTTOMLEFT_TOPRIGHT],
             *   - [Whisper.Enums.GradientOrientation.TOPLEFT_BOTTOMRIGHT]
             */
            var gradientOrientation: Whisper.Enums.GradientOrientation = defaultGradientOrientation

            /**
             * The X position of the center of the gradient.
             * Only used if [BackgroundTemplate.type] is [Whisper.Enums.BackgroundType.GRADIENT_RADIAL] or [Whisper.Enums.BackgroundType.GRADIENT_SWEEP].
             */
            var gradientCenterX: Float = Constants.DEFAULT_GRADIENT_CENTER_X
                get() = field.zeroOrGreater(Constants.DEFAULT_GRADIENT_CENTER_X)

            /**
             * The Y position of the center of the gradient.
             * Only used if [BackgroundTemplate.type] is [Whisper.Enums.BackgroundType.GRADIENT_RADIAL] or [Whisper.Enums.BackgroundType.GRADIENT_SWEEP].
             */
            var gradientCenterY: Float = Constants.DEFAULT_GRADIENT_CENTER_Y
                get() = field.zeroOrGreater(Constants.DEFAULT_GRADIENT_CENTER_Y)

            /**
             * Sets the radius of the gradient.
             * Only used by [Whisper.Enums.BackgroundType.GRADIENT_RADIAL].
             */
            var gradientRadius: Float = Constants.DEFAULT_GRADIENT_RADIUS
                get() = field.zeroOrGreater(Constants.DEFAULT_GRADIENT_RADIUS)

            /** non-public field used by Whisper to convert the user defined color hex codes to a color ints. */
            internal val colorsInt: IntArray
                get() = colors.toColorIntArray()
        }

        class BorderTemplate {
            /** Whisper corner radius (corner roundness). */
            val cornerRadius: CornerRadius = CornerRadius()

            /** Whisper border width (size). */
            var size: Int = Constants.DEFAULT_BORDER_STOKE_WIDTH
                get() = field.zeroOrGreater(Constants.DEFAULT_BORDER_STOKE_WIDTH)

            /**
             * Whisper border color.
             * Value must be a 6 or 8 character color hex code as "RRGGBB" or "AARRGGBB".
             */
            var color: String = Constants.DEFAULT_BORDER_STROKE_COLOR

            /** non-public field used by Whisper to convert the user defined color hex code to a color int. */
            internal val colorInt: Int
                get() = color.toColorInt(Constants.DEFAULT_BORDER_STROKE_COLOR)
        }

        class ShadowTemplate {
            /**
             * Display a shadow for each Whisper.
             * If true enables all fields in [ShadowTemplate] (otherwise they are all ignored).
             */
            var castShadow: Boolean = DEFAULT_CAST_SHADOW

            /**
             * Shadow color.
             * Value must be a 6 or 8 character color hex code as "RRGGBB" or "AARRGGBB".
             */
            var color: String = Constants.DEFAULT_SHADOW_COLOR

            /** Shadow corner radius (corner roundness) */
            val cornerRadius: CornerRadius = CornerRadius()

            /**
             * Shadow insets (left, top, right, bottom).
             * Shadow effect is achieved when [ShadowTemplate.inset] is used in combination with [ShadowTemplate.padding].
             */
            val inset: PaddingTemplate = PaddingTemplate(
                left = Constants.DEFAULT_SHADOW_INSET_LEFT,
                top = Constants.DEFAULT_SHADOW_INSET_TOP,
                right = Constants.DEFAULT_SHADOW_INSET_RIGHT,
                bottom = Constants.DEFAULT_SHADOW_INSET_BOTTOM
            )

            /**
             * Shadow padding (left, top, right, bottom).
             * Shadow effect is achieved when [ShadowTemplate.inset] is used in combination with [ShadowTemplate.padding].
             * */
            val padding: PaddingTemplate = PaddingTemplate(
                left = Constants.DEFAULT_SHADOW_PADDING_LEFT,
                top = Constants.DEFAULT_SHADOW_PADDING_TOP,
                right = Constants.DEFAULT_SHADOW_PADDING_RIGHT,
                bottom = Constants.DEFAULT_SHADOW_PADDING_BOTTOM
            )

            /** non-public field used by Whisper to convert the user defined color hex code to a color int. */
            internal val colorInt: Int
                get() = color.toColorInt(Constants.DEFAULT_SHADOW_COLOR)
        }
    }
}