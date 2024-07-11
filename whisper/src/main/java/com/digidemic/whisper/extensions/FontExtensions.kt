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
import android.graphics.Typeface
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Common extension functions for Whisper to consume internally that are related to fonts.
 */
internal object FontExtensions {

    /** Any font retrieved is cached here for quicker future use. */
    private val fontCache = ConcurrentHashMap<String, Typeface>()

    // ConcurrentHashMap does not allow null as a value. Because of this, a separate collection keeps track of keys will invalid typefaces to not have to try to create from asset each time that will fail.
    /** Because ConcurrentHashMap does not allow null as a value, which is [fontCache]'s type,
     * [invalidFonts] is a separate collection of fonts failed to retrieve from [getFont].
     * This collection is checked against before attempting retrieve a font typeface from [getFont].
     * If the font is found in this collection, no further attempting to retrieve the font from [getFont] will be done.
     */
    private val invalidFonts: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

    /**
     * Takes a font file located in the module's /assets/ directory and returns the font typeface.
     * Results are cached for quicker future processing if this function is called again at a later point with the same [fontNameAndExtension].
     *
     * @param fontNameAndExtension The name of the font file with its extension found in the module's /assets/ directory.
     *   Example: [fontNameAndExtension] would be "Miracode.ttf" if within src/main/assets/Miracode.ttf
     */
    fun Activity.getFont(fontNameAndExtension: String?): Typeface? =
        fontNameAndExtension?.let { font ->
            fontCache[font] ?: try {
                if(invalidFonts.contains(font)) {
                    return@let null
                }
                Typeface.createFromAsset(assets, font)
            } catch (_: Exception) {
                null
            }.also { typeface ->
                if(typeface == null) {
                    invalidFonts.add(font)
                } else {
                    fontCache[font] = typeface
                }
            }
        }
}