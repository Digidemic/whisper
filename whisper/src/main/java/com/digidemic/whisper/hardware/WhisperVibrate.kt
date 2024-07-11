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

package com.digidemic.whisper.hardware

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.digidemic.whisper.ProfileTemplate
import com.digidemic.whisper.Whisper

/**
 * Vibrate functions for Whisper to consume internally.
 */
internal object WhisperVibrate {

    /**
     * Vibrate with defined [ProfileTemplate.VibrateTemplate.vibrationPattern] if
     * [ProfileTemplate.VibrateTemplate.trigger] is not [Whisper.Enums.TriggerVibrate.NEVER].
     */
    fun runOrSkip(
        activity: Activity,
        profile: ProfileTemplate,
        whisperCount: Int
    ) {
        when(profile.vibrate.trigger) {
            Whisper.Enums.TriggerVibrate.NEVER -> return
            Whisper.Enums.TriggerVibrate.EVERY_WHISPER -> vibrateNow(activity, profile)
            Whisper.Enums.TriggerVibrate.SOLE_WHISPER ->
                if(whisperCount == 0) {
                    vibrateNow(activity, profile)
                }
        }
    }

    /** Vibrate with defined [ProfileTemplate.VibrateTemplate.vibrationPattern] */
    @Suppress("MissingPermission", "DEPRECATION")
    private fun vibrateNow(
        activity: Activity,
        profile: ProfileTemplate
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrate(VibrationEffect.createWaveform(profile.vibrate.vibrationPattern, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrate(profile.vibrate.vibrationPattern, -1)
                }
            }
        } catch (_: Exception) {
            // Could not properly test all OS scenarios. No sense having an entire app crash if vibration were to fail.
        }
    }
}