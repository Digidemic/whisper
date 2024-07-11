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

import android.graphics.Color
import androidx.annotation.ColorInt
import com.digidemic.whisper.Constants
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Throws

/**
 * Common extension functions for Whisper to consume internally that are related to color.
 */
internal object ColorExtensions {

    /** Any hex color string converted into a color int is cached here for quicker future use. */
    private val colorCache = ConcurrentHashMap<String, Int>()

    /**
     * Converts a hex color string into a color int.
     * Results are cached for quicker future processing if this function is called again at a later point with the same hex color strings.
     *
     * @param defaultColor OPTIONAL, if hex color string fails to parse into a color int, the [defaultColor] hex string will be parsed.
     *   If [defaultColor] is undefined or fails to convert, [Constants.DEFAULT_PARSING_FAILING_COLOR] is used.
     * @return Converted color int.
     */
    fun String.toColorInt(defaultColor: String? = null): Int =
        uppercase().let { hexColor ->
            colorCache[hexColor] ?: try {
                hexToColor()
            } catch (_: Exception) {
                defaultColor?.hexToColor(Constants.DEFAULT_PARSING_FAILING_COLOR)
                    ?: Constants.DEFAULT_PARSING_FAILING_COLOR
            }.also {
                colorCache[hexColor] = it
            }
        }

    /**
     * Converts a collection of hex color string into an [IntArray] of color ints.
     * Uses [toColorInt] to parse each hex color string which uses [colorCache] for potentially faster results.
     * If collection is empty or there is a failure converting the collection, a single int array of [Constants.DEFAULT_PARSING_FAILING_COLOR]
     * is returned.
     * @return Converted color ints.
     */
    fun List<*>.toColorIntArray(): IntArray {
        ifEmpty {
            return arrayOf(Constants.DEFAULT_PARSING_FAILING_COLOR).toIntArray()
        }
        try {
            val colors = mutableListOf<Int>()
            forEach { colorHex ->
                colors.add(colorHex.toString().toColorInt())
            }
            return colors.toIntArray()
        } catch (_: Exception) {
            return arrayOf(Constants.DEFAULT_PARSING_FAILING_COLOR).toIntArray()
        }
    }

    /**
     * Attempts to parse a hex color string to a color int.
     * If string fails to parse, [IllegalArgumentException] is thrown.
     * An overloaded [hexToColor] function exists requiring a defaultColor parameter
     * so the function will never thrown an exception (unlike the possibility of this one).
     * @throws IllegalArgumentException if parsing hex color string fails.
     */
    @Throws(IllegalArgumentException::class)
    private fun String.hexToColor(): Int {
        val color = apply {
            // If a hex color string start with "#", remove it as it will be re-added regardless during the color parse.
            if(startsWith("#")){
                drop(0)
            }
            // Make sure color is of proper length before parsing. If not, full in the gaps.
        }.fullHexLength()
        return Color.parseColor("#$color")
    }

    /**
     * Overloaded [hexToColor] function that includes a [defaultColor]
     * parameter so the function will never throw an exception.
     *
     * @param defaultColor a [ColorInt] which will be returned if parsing the hex color string fails.
     */
    private fun String.hexToColor(@ColorInt defaultColor: Int): Int =
        try {
            hexToColor()
        } catch (_: Exception) {
            defaultColor
        }

    /**
     * Validates and updates a hex color string if it does not the proper length size for "RRGGBB" or "AARRGGBB".
     *
     * Using [com.digidemic.kyaml.Kyaml] to parse whisper.yaml, if a color string values does not contain
     * quotes and begins with 0s, those 0s may be removed (when they are needed).
     * This is because Kyaml detects and parses the value type before returning it.
     * So if the value begins with 0s but also has letters, it will be viewed as a String and the 0s will not be removed.
     *   Example: 04AB12CD -> "04AB12CD"
     * But if the value begins with 0s but only contains numbers, it will be viewed as an Integer (or Long) and the 0s will be removed.
     *   Example1: 010101 -> 10101
     *   Example2: "010101" -> "010101"
     * While the whisper.yaml template has quotes around each hex color string, to avoid confusion,
     * the length check and including of prefixed 0s exists here to better avoid any mistakes.
     */
    private fun String.fullHexLength(): String =
        when {
            length == 7 -> "0$this"
            length <= 5 -> {
                var updatedColor = this
                for (i in this.length + 1..6) {
                    updatedColor = "0$updatedColor"
                }
                updatedColor
            }
            else -> this
        }
}