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

import android.annotation.SuppressLint
import android.app.Activity
import com.digidemic.whisper.Whisper

/**
 * Common extension functions for Whisper to consume internally that are related to the device's status bar.
 */
internal object StatusBarExtensions {

    /** Once the device's status bar height is discovered from [statusBarHeight], the value is cached here for quicker future use. */
    @Volatile
    private var statusBarHeightCache: Int? = null

    // For Whispers just below the android status bar, account for the height of the status bar so Y offset can be entered correctly.
    /**
     * Returns the device's status bar height in pixels.
     * This is needed when [Whisper.GlobalConfig.Offset.additionalOffsetForStatusBar] for Whispers set to show just below the status bar (when visible).
     * See [Whisper.GlobalConfig.Offset.additionalOffsetForStatusBar] for more information about why this is needed.
     */
    fun Activity.statusBarHeight(): Int =
        statusBarHeightCache ?: try {
            with(resources) {
                @SuppressLint("DiscouragedApi", "InternalInsetResource")
                val statusBarHeightId = getIdentifier(
                    "status_bar_height",
                    "dimen",
                    "android"
                )
                getDimensionPixelSize(statusBarHeightId).let {
                    if (it >= 0) it else 0
                }
            }
        } catch (_: Exception) {
            0
        }.also {
            statusBarHeightCache = it
        }
}