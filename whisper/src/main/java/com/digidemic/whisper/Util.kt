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

import android.view.View
import com.digidemic.whisper.extensions.extractViewNameFromTag
import com.digidemic.whisper.extensions.extractWhisperIdFromTag

/**
 * Common miscellaneous utility functions
 */
internal object Util {

    /** synchronized locking variable for [traverseWhispers]  */
    private val syncLockTraverseWhispers = Any()

    /** With the current configurations, could the active auto-closing timer be set for the current Whisper. */
    fun whisperMayBeUsingTimeout(duration: Long): Boolean =
        duration > 0 && Whisper.GlobalConfig.timeoutOnlyForOldestWhisper

    /** With the current configurations, is the auto-closing timer running for the current Whisper. */
    fun isWhisperUsingTimeout(duration: Long, order: Int): Boolean =
        order == 1 && whisperMayBeUsingTimeout(duration)

    /** Loops through the Whisper Window layout counting every child view with the [Constants.WHISPER_VIEW_NAME_TAG] value in its tag (indicating it is a Whisper view). */
    fun traverseWhispers(action: (Int, String, View) -> Unit) {
        synchronized(syncLockTraverseWhispers) {
            val reverseTraversal = Whisper.GlobalConfig.sortOrder == Whisper.Enums.SortOrder.ABOVE
            var elementTagCount = 0
            val whisperCount = WhisperWindow.windowLayout?.childCount ?: 0
            for (i in whisperCount.iterateFullCollection(reverseTraversal)) {
                val view = WhisperWindow.windowLayout?.getChildAt(i)
                if(view?.extractViewNameFromTag() == Constants.WHISPER_VIEW_NAME_TAG) {
                    elementTagCount += 1
                    action(elementTagCount, view.extractWhisperIdFromTag(), view)
                }
            }
        }
    }

    /**
     * Used in the [traverseWhispers] loop to iterate forward or reverse through every element in the collection.
     */
    private fun Int.iterateFullCollection(inReverse: Boolean): IntProgression {
        return if(inReverse) {
            IntProgression.fromClosedRange(this - 1, 0, -1)
        } else {
            IntProgression.fromClosedRange(0, this - 1, 1)
        }
    }
}