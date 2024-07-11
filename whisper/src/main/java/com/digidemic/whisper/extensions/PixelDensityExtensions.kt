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
import android.util.TypedValue
import com.digidemic.whisper.Whisper
import kotlin.math.roundToInt

/**
 * Common extension functions for Whisper to consume internally that are related to pixel density.
 */
internal object PixelDensityExtensions {

    /** Once the pixels per DP is discovered for the device, from [toPx], the value is cached here for quicker future use. */
    @Volatile
    private var pixelsPerDpCache: Float? = null

    /**
     * Takes a size [value] and converts it to the pixel density measurement defined in [Whisper.GlobalConfig.pixelDensityUnit].
     *
     * @param value a size value.
     * @return converted [value] into the defined [Whisper.GlobalConfig.pixelDensityUnit]
     */
    fun Activity.toPixelDensityUnit(value: Int): Int =
        when(Whisper.GlobalConfig.pixelDensityUnit) {
            Whisper.Enums.PixelDensity.DP -> toPx(value)
            Whisper.Enums.PixelDensity.PX -> value
        }

    /**
     * Takes a [dp] value and converts it into pixels.
     *
     * @param dp the value to convert into pixels.
     * @return the converted [dp] into pixels.
     */
    private fun Activity.toPx(dp: Int): Int =
        (pixelsPerDpCache
            ?: TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                resources.displayMetrics
            ).also { pixelsPerDp ->
                pixelsPerDpCache = pixelsPerDp
            }
        ).let { pixelsPerDp ->
            (dp * pixelsPerDp).roundToInt()
        }
}