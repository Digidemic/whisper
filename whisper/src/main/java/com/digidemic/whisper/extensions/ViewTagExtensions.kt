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
import com.digidemic.whisper.Constants

/**
 * Delimited by [Constants.WHISPER_VIEW_TAG_DELIMITER], returns the first index (0) value from the view tag.
 * If found, the value should be the view name associated with the Whisper view.
 */
internal fun View.extractViewNameFromTag(): String? = extractValueFromTag(0)

/**
 * Delimited by [Constants.WHISPER_VIEW_TAG_DELIMITER], returns the second index (1) value from the view tag.
 * If found, the value should be the Whisper ID associated with the Whisper view.
 * If this call fails, [Constants.DEFAULT_WHISPER_ID] is returned.
 */
internal fun View.extractWhisperIdFromTag(): String = extractValueFromTag(1) ?: Constants.DEFAULT_WHISPER_ID

/**
 * Delimited by [Constants.WHISPER_VIEW_TAG_DELIMITER], returns the third index (2) value from the view tag.
 * If found, the value should be the Whisper auto-removal timeout duration associated with the Whisper view.
 * If this call or parsing into a [Long] fails, the passed [defaultDuration] parameter is returned.
 */
internal fun View.extractDurationFromTag(defaultDuration: Long): Long =
    extractValueFromTag(2)
        ?.toLongOrNull()
        ?: defaultDuration

/**
 * Takes a view's tag as a string, splits it with [Constants.WHISPER_VIEW_TAG_DELIMITER], then returns the value of the passed [index].
 * If a values does not exist for that [index], return null.
 *
 * @param index request index from the view tag split by [Constants.WHISPER_VIEW_TAG_DELIMITER].
 * @return the [index] value found after splitting the view's tag. If there is no value for that [index], return null.
 */
private fun View.extractValueFromTag(index: Int): String? =
    tag
        ?.toString()
        ?.split(Constants.WHISPER_VIEW_TAG_DELIMITER)
        ?.getOrNull(index)