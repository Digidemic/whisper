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
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.digidemic.whisper.Constants.DEFAULT_WHISPER_MARGIN
import com.digidemic.whisper.Constants.WHISPER_VIEW_TAG_DELIMITER
import com.digidemic.whisper.Constants.WHISPER_VIEW_NAME_TAG
import com.digidemic.whisper.extensions.FontExtensions.getFont
import com.digidemic.whisper.extensions.LayoutSizingExtensions.linearLayoutParamsDynamicWidth
import com.digidemic.whisper.extensions.LayoutSizingExtensions.setMargins
import com.digidemic.whisper.extensions.WhisperViewExtensions.addWhisper
import com.digidemic.whisper.extensions.WhisperViewExtensions.refreshTimer
import com.digidemic.whisper.extensions.WhisperViewExtensions.removeWhisper
import com.digidemic.whisper.hardware.WhisperSound
import com.digidemic.whisper.hardware.WhisperVibrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Creates a new Whisper using the passed in parameters, [Whisper.GlobalConfig], and [ProfileTemplate].
 *
 * @param activity active Activity. Needed for displaying the Whisper and finding if app is running in debug.
 * @param whisperId a unique ID (UUID) to associate with the Whisper. [whisperId] is added to the view's tag.
 * @param message text to display within the Whisper.
 * @param duration millisecond value the Whisper will display for before auto-closing itself.
 *   See [Whisper.GlobalConfig.timeoutOnlyForOldestWhisper] for additional conditions.
 * @param profile what profile was called to create the Whisper.
 *   The profile type will determine what set of configurations to use from [ProfileTemplate].
 *   Profile functions that may have been called are [Whisper.default], [Whisper.info],
 *   [Whisper.warn], [Whisper.error], [Whisper.fatal], [Whisper.critical], [Whisper.trace], or [Whisper.debug].
 * @param onClick will execute the passed block of code when tapped (if defined).
 *   Will also close the individual Whisper when tapped regardless if [onClick] if defined (as long as [Whisper.GlobalConfig.tapToDismiss] is true).
 *   Available withing [onClick] will be the Activity and its unique Whisper ID.
 *   NOTE: If a the Whisper does not have a timeout ([duration] passed was 0) and [Whisper.GlobalConfig.tapToDismiss] is set to false,
 *   the Whisper can still be dismissed in the [onClick] by passing the unique Whisper ID to [Whisper.remove].
 */
internal class BuildNewWhisper(
    private val activity: Activity,
    private val whisperId: String,
    private val message: String,
    private val duration: Long,
    private val profile: ProfileTemplate,
    private val onClick: ((Activity, String) -> Unit)? = null,
): WhisperWindow(activity) {

    /** Flag set if Whisper was clicked */
    @Volatile
    private var clicked: Boolean = false

    /** Lazily builds the Whisper View before adding it to the Whisper Window. */
    private val whisperView: View by lazy {
        TextView(activity).apply {

            // Set Whisper width and height.
            layoutParams = linearLayoutParamsDynamicWidth

            // Set Whisper's view tag. Ex: WhisperView|165be090-e382-49e1-9ec8-4ceaac4e3617|3000
            tag = "$WHISPER_VIEW_NAME_TAG$WHISPER_VIEW_TAG_DELIMITER$whisperId$WHISPER_VIEW_TAG_DELIMITER$duration"

            // Fade in animation.
            animation = fadeInAnimation()

            // Message text.
            text = message

            // Space (gap) between each Whisper if multiple are being displayed on screen.
            // The space is defined by a margin either above or below the Whisper depending on [Whisper.GlobalConfig.sortOrder].
            if(Whisper.activeWhisperCount() > 0) {
                when(Whisper.GlobalConfig.sortOrder) {
                    Whisper.Enums.SortOrder.ABOVE -> setMargins(
                        DEFAULT_WHISPER_MARGIN.toPixelDensityUnit(),
                        DEFAULT_WHISPER_MARGIN.toPixelDensityUnit(),
                        DEFAULT_WHISPER_MARGIN.toPixelDensityUnit(),
                        Whisper.GlobalConfig.displaySpace.toPixelDensityUnit(),
                    )
                    Whisper.Enums.SortOrder.BELOW -> setMargins(
                        DEFAULT_WHISPER_MARGIN.toPixelDensityUnit(),
                        Whisper.GlobalConfig.displaySpace.toPixelDensityUnit(),
                        DEFAULT_WHISPER_MARGIN.toPixelDensityUnit(),
                        DEFAULT_WHISPER_MARGIN.toPixelDensityUnit()
                    )
                }
            }

            with(profile.design) {

                // Horizontal alignment of text
                gravity = text.gravity.gravity

                // Text color
                setTextColor(text.colorInt)

                // Text size
                textSize = text.size

                // Text bolded, italicized, both, or neither
                val fontStyle = when {
                    text.font.bold && text.font.italic -> Typeface.BOLD_ITALIC
                    text.font.bold -> Typeface.BOLD
                    text.font.italic -> Typeface.ITALIC
                    else -> null
                }

                // If there is a font family (Typeface) for the text.
                val fontFamily = activity.getFont(text.font.fontFamily)

                // Apply font family and/or font style (bold/italic) if either are defined.
                if (fontFamily != null || fontStyle != null) {
                    val selFontFamily = fontFamily ?: typeface
                    if (fontStyle != null) {
                        // Set both font family and font style.
                        setTypeface(selFontFamily, fontStyle)
                    } else {
                        // Set font family to typeface
                        typeface = selFontFamily
                    }
                }

                // If text should be underlined.
                if (text.font.underline) {
                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }

                // The shadow padding is used in a few places. Get each side converted to its proper pixel density (or 0 if disabled).
                val shadowPaddingLeft = if (shadow.castShadow) shadow.padding.left.toPixelDensityUnit() else 0
                val shadowPaddingTop = if (shadow.castShadow) shadow.padding.top.toPixelDensityUnit() else 0
                val shadowPaddingRight = if (shadow.castShadow) shadow.padding.right.toPixelDensityUnit() else 0
                val shadowPaddingBottom = if (shadow.castShadow) shadow.padding.bottom.toPixelDensityUnit() else 0

                /*
                To keep text centered within then Whisper, the following values need to be added each side of the Whisper padding (in the proper pixel density):
                    left, top, right, bottom of:
                      Whisper padding,
                      Shadow padding (if enabled. Value will be 0 if disabled).
                      Border size (This ensures the main body has the proper spacing between the text. Otherwise too large of a border will creep up on the main Whisper content).
                 */
                setPadding(
                    padding.left.toPixelDensityUnit() + shadowPaddingLeft + border.size.toPixelDensityUnit(),
                    padding.top.toPixelDensityUnit() + shadowPaddingTop + border.size.toPixelDensityUnit(),
                    padding.right.toPixelDensityUnit() + shadowPaddingRight + border.size.toPixelDensityUnit(),
                    padding.bottom.toPixelDensityUnit() + shadowPaddingBottom + border.size.toPixelDensityUnit()
                )

                // Define and add the Shadow (only if enabled) and main Whisper view to a list of GradientDrawable
                val drawableLayers = mutableListOf<GradientDrawable>()
                if (shadow.castShadow) {
                    drawableLayers.add(
                        GradientDrawable().apply {
                            mutate()
                            shape = GradientDrawable.RECTANGLE
                            setColor(shadow.colorInt)
                            cornerRadii = shadow.cornerRadius.radii.map { it.roundToInt().toPixelDensityUnit().toFloat() }.toFloatArray()
                        }
                    )
                }

                drawableLayers.add(
                    GradientDrawable().apply {
                        mutate()
                        shape = GradientDrawable.RECTANGLE
                        setStroke(border.size.toPixelDensityUnit(), border.colorInt)
                        cornerRadii = border.cornerRadius.radii.map { it.roundToInt().toPixelDensityUnit().toFloat() }.toFloatArray()

                        // "java.lang.IllegalArgumentException: needs >= 2 number of colors". Default to [Solid] if less than 2 colors.
                        background.type = if(background.colors.size <= 1) Whisper.Enums.BackgroundType.SOLID else background.type
                        when (background.type) {
                            Whisper.Enums.BackgroundType.SOLID ->
                                // If multiple colors are in [.background.colors], but [.background.type] is [SOLID], only use the first color in the list.
                                setColor(background.colorsInt.first())
                            Whisper.Enums.BackgroundType.GRADIENT_RADIAL,
                            Whisper.Enums.BackgroundType.GRADIENT_SWEEP,
                            Whisper.Enums.BackgroundType.GRADIENT_LINEAR -> {
                                // Apply gradient settings if Whisper background is any gradient background type.
                                gradientType = background.type.gradient
                                setGradientCenter(
                                    background.gradientCenterX,
                                    background.gradientCenterY,
                                )
                                gradientRadius = background.gradientRadius.roundToInt().toPixelDensityUnit().toFloat()

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    colors = background.colorsInt
                                    orientation = background.gradientOrientation.orientation
                                } else {
                                    val gd = GradientDrawable(
                                        background.gradientOrientation.orientation,
                                        background.colorsInt
                                    )
                                    gd.mutate()
                                    gd.gradientType = background.type.gradient
                                    gd.shape = GradientDrawable.RECTANGLE
                                    @Suppress("DEPRECATION")
                                    setBackgroundDrawable(gd)
                                }
                            }
                        }
                    }
                )

                // Add Shadow/Main Whisper layers to a LayerDrawable to perform further location adjustments before applying it.
                val layerDrawable = LayerDrawable(drawableLayers.toTypedArray())

                // If a shadow is present, need to shift the inset of foreground and shadow to keep Whisper location and text centered.
                if (shadow.castShadow && drawableLayers.count() >= 2) {
                    // Set shadow layer insets to adjust the shadow.
                    layerDrawable.setLayerInset(
                        0,
                        shadow.inset.left.toPixelDensityUnit(),
                        shadow.inset.top.toPixelDensityUnit(),
                        shadow.inset.right.toPixelDensityUnit(),
                        shadow.inset.bottom.toPixelDensityUnit()
                    )

                    // Main Whisper layer
                    layerDrawable.setLayerInset(
                        1,
                        shadowPaddingLeft,
                        shadowPaddingTop,
                        shadowPaddingRight,
                        shadowPaddingBottom
                    )
                }

                // Apply Shadow/Main Whisper layers as the background.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    this@apply.background = layerDrawable
                } else {
                    @Suppress("DEPRECATION")
                    this@apply.setBackgroundDrawable(layerDrawable)
                }
            }

            // If a Whisper is tapped.
            setOnTouchListener { view, motionEvent ->

                // Allow a Whisper to only be clicked once.
                //   Otherwise the user can repeatedly tap the same Whisper keeping it from closing
                if (!clicked) {
                    clicked = true

                    // If an [onClick] code block was passed in
                    onClick?.let { click ->
                        try {
                            // Run the block of code passed to the [onClick].
                            click(activity, whisperId)
                        } catch (_: Exception) { /* do nothing */ }
                    }

                    // Regardless if an [onClick] was passed in, begin the Whisper closing process if [Whisper.GlobalConfig.tapToDismiss] is true.
                    if (Whisper.GlobalConfig.tapToDismiss) {

                        // If the oldest Whisper was tapped and has set auto-removal timeout (its duration is greater than 0),
                        //   pass in true to cancel the coroutine timer so it can be reset.
                        var cancelTimer = false
                        if (Util.whisperMayBeUsingTimeout(duration)) {
                            Util.traverseWhispers { order, _, selView ->
                                if (Util.isWhisperUsingTimeout(duration, order) && selView == whisperView) {
                                    cancelTimer = true
                                }
                            }
                        }

                        // Start the removal process by fading out the tapped Whisper.
                        whisperView.removeWhisper(activity, cancelTimer)
                    }
                    view.performClick()
                }
                view.onTouchEvent(motionEvent)
            }
        }
    }

    init {
        // Synchronized to allow inherited [WhisperWindow] to complete first the event multiple Whispers need to be created at once.
        synchronized(this) {
            // Without CoroutineScope(Dispatchers.Main) (or runOnUiThread), if multiple Whispers need to be created at once, the order may be incorrect.
            CoroutineScope(Dispatchers.Main).launch {
                // Play sound or vibrate while creating Whisper if either are enabled.
                Whisper.activeWhisperCount().apply {
                    WhisperSound.runOrSkip(activity, profile, this)
                    WhisperVibrate.runOrSkip(activity, profile, this)
                }
                // Create Whisper and add it to the Whisper Window to be displayed.
                whisperView.addWhisper()
                // If newly created Whisper has a set auto-remove duration, may need to start the auto-timer immediately for the new Whisper.
                // This may depend if [Whisper.GlobalConfig.timeoutOnlyForOldestWhisper] is enabled and if the new Whisper is now the oldest one being displayed.
                if (duration > 0) {
                    whisperView.refreshTimer(activity)
                }
            }
        }
    }

    /** Fade in animation. */
    private fun fadeInAnimation() = AnimationSet(false).apply {
        addAnimation(
            AlphaAnimation(0f, 1f).apply {
                interpolator = DecelerateInterpolator()
                duration = Whisper.GlobalConfig.animationTransitionDuration
            }
        )
    }
}