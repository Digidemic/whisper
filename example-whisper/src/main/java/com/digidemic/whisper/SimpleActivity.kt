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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digidemic.whisper.MainActivity.Companion.DARK_THEME_KEY
import com.digidemic.whisper.ui.theme.WhisperTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SimpleActivity : ComponentActivity()  {

    override fun onDestroy() {
        super.onDestroy()
        Whisper.finish(this@SimpleActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val whisperDelay = 200L
            var darkTheme: Boolean by remember { mutableStateOf(intent.extras?.getBoolean(DARK_THEME_KEY) ?: true) }
            val message: String by remember { mutableStateOf(intent.extras?.getString(MainActivity.SIMPLE_VIEW_MESSAGE_KEY) ?: "Whisper clicked!") }
            var allWhisperButtonEnabled by remember { mutableStateOf(true) }

            WhisperTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            onClick = {
                                allWhisperButtonEnabled = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    Whisper(this@SimpleActivity, message)
                                    delay(whisperDelay)
                                    Whisper.info(this@SimpleActivity, message)
                                    delay(whisperDelay)
                                    Whisper.warn(this@SimpleActivity, message)
                                    delay(whisperDelay)
                                    Whisper.error(this@SimpleActivity, message)
                                    delay(whisperDelay)
                                    Whisper.critical(this@SimpleActivity, message)
                                    delay(whisperDelay)
                                    Whisper.fatal(this@SimpleActivity, message)
                                    delay(whisperDelay)
                                    Whisper.trace(this@SimpleActivity, message)
                                    delay(whisperDelay)
                                    Whisper.debug(this@SimpleActivity, message)
                                    allWhisperButtonEnabled = true
                                }
                            },
                            enabled = allWhisperButtonEnabled
                        ) {
                            Text(text = "All Whispers!")
                        }

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            onClick = {
                                darkTheme = !darkTheme
                            }) {
                            Text(text =
                            if(darkTheme) {
                                "Light Mode"
                            } else {
                                "Dark Mode"
                            })
                        }
                    }
                }
            }
        }
    }
}