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

import android.view.View
import android.widget.LinearLayout
import com.digidemic.whisper.R
import com.digidemic.whisper.Whisper
import com.digidemic.whisper.WhisperWindow

/**
 * Common extension functions for Whisper to consume internally that are related to layout sizing.
 */
internal object LayoutSizingExtensions {

    /**
     * If view already has LayoutParams defined, return it as is.
     * Otherwise return a [LinearLayout.LayoutParams] where width
     * and height are both [LinearLayout.LayoutParams.WRAP_CONTENT].
     */
    val View.linearLayoutParams: LinearLayout.LayoutParams
        get() = (layoutParams as? LinearLayout.LayoutParams) ?: LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

    /**
     * If view already has LayoutParams defined, return it as is.
     * Otherwise return a [LinearLayout.LayoutParams] where width is [layoutParamWidth]
     * and height is [LinearLayout.LayoutParams.WRAP_CONTENT].
     */
    val View.linearLayoutParamsDynamicWidth: LinearLayout.LayoutParams
        get() = (layoutParams as? LinearLayout.LayoutParams) ?: LinearLayout.LayoutParams(
            layoutParamWidth,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

    /**
     * If [Whisper.GlobalConfig.positionOnScreen] is [Whisper.Enums.PositionOnScreen.TOP_FULL] return
     * [LinearLayout.LayoutParams.MATCH_PARENT] otherwise return [LinearLayout.LayoutParams.WRAP_CONTENT].
     */
    internal val layoutParamWidth: Int
        get() = if(Whisper.GlobalConfig.positionOnScreen == Whisper.Enums.PositionOnScreen.TOP_FULL) {
            LinearLayout.LayoutParams.MATCH_PARENT
        } else {
            LinearLayout.LayoutParams.WRAP_CONTENT
        }

    /**
     * If [Whisper.GlobalConfig.positionOnScreen] is a value where Whispers will be displayed anywhere at the top of the screen.
     * At this time, top options are the only options. A future update plans to add more locations to display Whispers.
     */
    internal val locationIsTop: Boolean
        get() = when(Whisper.GlobalConfig.positionOnScreen) {
            Whisper.Enums.PositionOnScreen.TOP_FULL,
            Whisper.Enums.PositionOnScreen.TOP_CENTER,
            Whisper.Enums.PositionOnScreen.TOP_LEFT,
            Whisper.Enums.PositionOnScreen.TOP_RIGHT,
            Whisper.Enums.PositionOnScreen.TOP_START,
            Whisper.Enums.PositionOnScreen.TOP_END -> true
        }

    /**
     * Apply an updated height to [R.id.whisperEndPadding], the invisible
     * padding element that changes size for smoother Whisper animation when they are moved.
     */
    fun Int.setEndingPaddingView() {
        WhisperWindow.windowLayout?.findViewById<View>(R.id.whisperEndPadding)?.apply {
            layoutParams = layoutParams.apply {
                layoutParams.height = this@setEndingPaddingView
            }
        }
    }

    /**
     * Applies the same margin value to all sides (left, top, right, bottom) for the view.
     */
    fun View.setMargins(allMarginSides: Int) =
        setMargins(
            allMarginSides,
            allMarginSides,
            allMarginSides,
            allMarginSides,
        )

    /**
     * Applies an updated margin value to any side(s) passed (left, top, right, bottom) for the view.
     */
    fun View.setMargins(
        left: Int?,
        top: Int?,
        right: Int?,
        bottom: Int?,
    ) {
        layoutParams = linearLayoutParams.apply {
            setMargins(
                left ?: leftMargin,
                top ?: topMargin,
                right ?: rightMargin,
                bottom ?: bottomMargin,
            )
        }
    }
}