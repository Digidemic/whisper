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

import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.media.RingtoneManager
import com.digidemic.whisper.Constants.RAW_DIRECTORY_NAME
import com.digidemic.whisper.ProfileTemplate
import com.digidemic.whisper.Whisper
import java.util.concurrent.ConcurrentHashMap

/**
 * Sound functions for Whisper to consume internally.
 */
internal object WhisperSound {

    /** Any sound identifier retrieved is cached here for quicker future use. */
    private val soundCache = ConcurrentHashMap<String, Int>()

    /**
     * The device's set [RingtoneManager.TYPE_NOTIFICATION] sound cached.
     * Used when [ProfileTemplate.SoundTemplate.trigger] is
     * [Whisper.Enums.TriggerSound.DEVICE_EVERY_WHISPER]
     * or [Whisper.Enums.TriggerSound.DEVICE_SOLE_WHISPER].
     */
    private val ringtoneUri by lazy {
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    /**
     * Play sound if [ProfileTemplate.SoundTemplate.trigger] is anything other than [Whisper.Enums.TriggerSound.NEVER].
     */
    fun runOrSkip(
        activity: Activity,
        profile: ProfileTemplate,
        activeWhisperCount: Int
    ) {
        when(profile.sound.trigger) {
            Whisper.Enums.TriggerSound.NEVER -> return
            Whisper.Enums.TriggerSound.CUSTOM_EVERY_WHISPER -> playCustomSoundNow(activity, profile)
            Whisper.Enums.TriggerSound.CUSTOM_SOLE_WHISPER ->
                if(activeWhisperCount == 0) {
                    playCustomSoundNow(activity, profile)
                }
            Whisper.Enums.TriggerSound.DEVICE_EVERY_WHISPER -> playDeviceSoundNow(activity)
            Whisper.Enums.TriggerSound.DEVICE_SOLE_WHISPER ->
                if(activeWhisperCount == 0) {
                    playDeviceSoundNow(activity)
                }
        }
    }

    /**
     * Play the defined [ProfileTemplate.SoundTemplate.customSound] sound.
     */
    private fun playCustomSoundNow(
        activity: Activity,
        profile: ProfileTemplate,
    ) {
        try {
            profile.sound.customSound?.let { customSound ->
                @SuppressLint("DiscouragedApi")
                val rawResId = soundCache[customSound]
                    ?: activity.resources.getIdentifier(
                        customSound,
                        RAW_DIRECTORY_NAME,
                        activity.packageName
                    ).also {
                        soundCache[customSound] = it
                    }

                MediaPlayer.create(activity.baseContext, rawResId)?.apply {
                    setOnCompletionListener {
                        reset()
                        release()
                    }
                    start()
                }
            }
        } catch (_: Exception) { /* do nothing */ }
    }

    /**
     * Play the device's set [RingtoneManager.TYPE_NOTIFICATION] sound.
     */
    private fun playDeviceSoundNow(activity: Activity) {
        try {
            RingtoneManager.getRingtone(activity, ringtoneUri).play()
        } catch (_: Exception) { /* do nothing */ }
    }
}