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
import android.content.pm.ApplicationInfo
import com.digidemic.whisper.Whisper

/**
 * Common extension functions for Whisper to consume internally that are related to build settings.
 */
internal object BuildExtensions {

    /** Sets flag if application is running a debug build. Only sets once then cached. */
    @Volatile
    private var isDebuggingCache: Boolean? = null

    /** If Whisper is profile DEBUG or TRACE, only continue if debugging. */
    fun Activity.isProperBuildWithProfile(profile: Whisper.Enums.Profile): Boolean =
        if(profile != Whisper.Enums.Profile.DEBUG && profile != Whisper.Enums.Profile.TRACE) {
            true
        } else {
            isDebugBuild()
        }

    /**
     * If app is running a non-release build.
     * Results are cached for quicker future processing if this function is called again at a later point.
     */
    private fun Activity.isDebugBuild(): Boolean =
        isDebuggingCache ?: try {
            applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (_: Exception) {
            // Possible for ApplicationInfo to throw a RuntimeException.
            false
        }.also {
            isDebuggingCache = it
        }
}