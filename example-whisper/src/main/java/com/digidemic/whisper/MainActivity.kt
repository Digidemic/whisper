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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.digidemic.whisper.ui.theme.WhisperTheme

private const val PROFILE_DEFAULT = "Default"
private const val PROFILE_INFO = "Info"
private const val PROFILE_ERROR = "Error"
private const val PROFILE_WARN = "Warn"
private const val PROFILE_FATAL = "Fatal"
private const val PROFILE_CRITICAL = "Critical"
private const val PROFILE_TRACE = "Trace"
private const val PROFILE_DEBUG = "Debug"

private val arrayOfProfiles = arrayOf(PROFILE_DEFAULT, PROFILE_INFO, PROFILE_ERROR, PROFILE_WARN, PROFILE_FATAL, PROFILE_CRITICAL, PROFILE_TRACE, PROFILE_DEBUG)

private val textModifier = Modifier.padding(4.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
class MainActivity : ComponentActivity() {

    private val selectedProfileStr = mutableStateOf(PROFILE_DEFAULT)
    private var selectedProfile: ProfileTemplate = Whisper.Profiles.default

    override fun onDestroy() {
        super.onDestroy()
        Whisper.finish(this@MainActivity)
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var darkTheme: Boolean by remember { mutableStateOf(false) }

            WhisperTheme(darkTheme = darkTheme) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {

                        var whisperMessageInput by rememberSaveable { mutableStateOf("Whisper message example!") }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            SectionTitle("Whisper Demo App")
                            SectionSubtitle("Test Whisper with your config changes.")
                            SectionSubtitle("Change individual settings or make changes to the yaml config file (example-whisper/src/main/assets/whisper.yaml) before launching the app!")
                            Spacer(modifier = Modifier.height(12.dp))
                            TextField(
                                value = whisperMessageInput,
                                onValueChange = { whisperMessageInput = it },
                                label = { Text("Whisper message to display.") }
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            FlowRow {
                                CallWhisper(PROFILE_DEFAULT) {
                                    Whisper.default(this@MainActivity, whisperMessageInput)
                                }

                                CallWhisper(PROFILE_INFO) {
                                    Whisper.info(this@MainActivity, whisperMessageInput)
                                }

                                CallWhisper(PROFILE_WARN) {
                                    Whisper.warn(this@MainActivity, whisperMessageInput)
                                }

                                CallWhisper(PROFILE_ERROR) {
                                    Whisper.error(this@MainActivity, whisperMessageInput)
                                }

                                CallWhisper(PROFILE_FATAL) {
                                    Whisper.fatal(this@MainActivity, whisperMessageInput)
                                }

                                CallWhisper(PROFILE_CRITICAL) {
                                    Whisper.critical(this@MainActivity, whisperMessageInput)
                                }

                                CallWhisper(PROFILE_TRACE) {
                                    Whisper.trace(this@MainActivity, whisperMessageInput)
                                }

                                CallWhisper(PROFILE_DEBUG) {
                                    Whisper.debug(this@MainActivity, whisperMessageInput)
                                }
                            }
                        }

                        Line()

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FlowRow {
                                Button(onClick = {
                                    darkTheme = !darkTheme
                                }) {
                                    Text(
                                        text =
                                        (if (darkTheme) "Light" else "Dark") + " Mode"
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(onClick = {
                                    val intent = Intent(this@MainActivity, SimpleActivity::class.java)
                                    intent.putExtra(DARK_THEME_KEY, darkTheme)
                                    intent.putExtra(SIMPLE_VIEW_MESSAGE_KEY, whisperMessageInput)
                                    startActivity(intent)
                                }) {
                                    Text(text = "Simple View")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(onClick = {
                                    Whisper.clear(this@MainActivity)
                                }) {
                                    Text(text = "Clear")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Line()

                        Spacer(modifier = Modifier.height(12.dp))

                        var configExpanded by remember { mutableStateOf(false) }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if(!configExpanded) {
                                Button(onClick = {
                                    configExpanded = true
                                }) {
                                    Text(text = "Update Configurations")
                                }
                            } else {
                                SectionTitle("Update configurations for:")
                                ConfigDDL()
                            }
                        }

                        if (configExpanded) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                    .padding(8.dp)
                            ) {
                                HorizontalPager(
                                    contentPadding = PaddingValues(
                                        horizontal = 32.dp,
                                    ),
                                    state = rememberPagerState(
                                        initialPage = 0,
                                        initialPageOffsetFraction = 0.05f,
                                    ) { 40 }
                                ) { page ->
                                    when(page) {

                                        0 -> StringSetting(
                                            title = "Text Color",
                                            textFieldLabel = "AARRGGBB Only",
                                            defaultInputValue = "FFFFFFFF",
                                            updateSetting = {
                                                applyColorOrDisplayWarning(it) { hex ->

                                                    selectedProfile.design.text.color = hex
                                                }
                                            },
                                        )

                                        1 -> FloatSetting(
                                            title = "Text Size",
                                            defaultInputValue = "20.0",
                                            updateSetting = { selectedProfile.design.text.size = it }
                                        )

                                        2 -> DropdownSetting(
                                            title = "Text Gravity",
                                            arr = arrayOf("LEFT", "RIGHT", "CENTER", "START", "END"),
                                            defaultInputValue = selectedProfile.design.text.gravity.toString(),
                                            updateSetting = {
                                                selectedProfile.design.text.gravity =
                                                    Whisper.Enums.TextGravity.valueOf(it)
                                            }
                                        )

                                        3 -> BooleanSetting(
                                            title = "Font Bolded",
                                            defaultInputValue = false,
                                            updateSetting = { selectedProfile.design.text.font.bold = it }
                                        )

                                        4 -> BooleanSetting(
                                            title = "Font Italicized",
                                            defaultInputValue = false,
                                            updateSetting = { selectedProfile.design.text.font.italic = it }
                                        )

                                        5 -> BooleanSetting(
                                            title = "Font Underlined",
                                            defaultInputValue = false,
                                            updateSetting = { selectedProfile.design.text.font.underline = it }
                                        )

                                        6 -> StringSetting(
                                            title = "Font Family",
                                            textFieldLabel = "Enter Acrylic.otf, Miracode.ttf, or Tachyo.otf",
                                            defaultInputValue = "Tachyo.otf",
                                            updateSetting = {
                                                selectedProfile.design.text.font.fontFamily = it
                                            },
                                        )

                                        7 -> StringSetting(
                                            title = "Background Color\nComma delimited if \"Background Type\" is a gradient",
                                            textFieldLabel = "AARRGGBB Only",
                                            defaultInputValue = "BB00FF00,BBFF00FF",
                                            updateSetting = { colors ->
                                                val colorList = colors
                                                    .filterNot { it.isWhitespace() }
                                                    .split(",")

                                                val invalidColors = mutableListOf<String>()
                                                colorList.forEach {
                                                    val isValidHex = isColorHexValid(it)
                                                    if(!isValidHex) {
                                                        invalidColors.add(it)
                                                    }
                                                }

                                                if(invalidColors.isEmpty()) {
                                                    selectedProfile.design.background.colors = colorList
                                                } else if(invalidColors.size == 1) {
                                                    displaySelectedProfileWhisper("${invalidColors.joinToString(",", "\"", "\"")} is an invalid color. AARRGGBB hex values only for colors. All changes were not applied.")
                                                } else {
                                                    displaySelectedProfileWhisper("${invalidColors.joinToString(",", "\"", "\"")} are invalid colors. AARRGGBB hex values only for colors. All changes were not applied.")
                                                }
                                            },
                                        )

                                        8 -> DropdownSetting(
                                            title = "Background Type\nIf a gradient, comma delimit \"Background Color\"",
                                            arr = arrayOf("SOLID", "GRADIENT_LINEAR", "GRADIENT_RADIAL", "GRADIENT_SWEEP"),
                                            defaultInputValue = selectedProfile.design.background.type.toString(),
                                            updateSetting = {
                                                selectedProfile.design.background.type = Whisper.Enums.BackgroundType.valueOf(it)
                                            }
                                        )

                                        9 -> ArrayLongSetting(
                                            title = "Padding\nComma delimited in order:\nleft,top,right,bottom",
                                            defaultInputValue = "8,8,8,8",
                                            updateSetting = {
                                                it.forEachIndexed { index, padding ->
                                                    when(index){
                                                        0 -> selectedProfile.design.padding.left = padding.toInt()
                                                        1 -> selectedProfile.design.padding.top = padding.toInt()
                                                        2 -> selectedProfile.design.padding.right = padding.toInt()
                                                        3 -> selectedProfile.design.padding.bottom = padding.toInt()
                                                    }
                                                }
                                            }
                                        )

                                        10 -> StringSetting(
                                            title = "Border Color",
                                            textFieldLabel = "AARRGGBB Only",
                                            defaultInputValue = "FFFFFFFF",
                                            updateSetting = {
                                                applyColorOrDisplayWarning(it) { hex ->
                                                    selectedProfile.design.border.color = hex
                                                }
                                            },
                                        )

                                        11 -> LongSetting(
                                            title = "Border Size",
                                            defaultInputValue = "4",
                                            updateSetting = { selectedProfile.design.border.size = it.toInt() }
                                        )

                                        12 -> ArrayLongSetting(
                                            title = "Corner Radius\nComma delimited in order:\nTopLeft,TopRight,BottomRight,BottomLeft",
                                            defaultInputValue = "8,8,8,8",
                                            updateSetting = {
                                                it.forEachIndexed { index, radius ->
                                                    when(index){
                                                        0 -> selectedProfile.design.border.cornerRadius.topLeft = radius.toFloat()
                                                        1 -> selectedProfile.design.border.cornerRadius.topRight = radius.toFloat()
                                                        2 -> selectedProfile.design.border.cornerRadius.bottomRight = radius.toFloat()
                                                        3 -> selectedProfile.design.border.cornerRadius.bottomLeft = radius.toFloat()
                                                    }
                                                }
                                            }
                                        )

                                        13 -> BooleanSetting(
                                            title = "Cast Shadow",
                                            defaultInputValue = true,
                                            updateSetting = {
                                                selectedProfile.design.shadow.castShadow = it
                                            }
                                        )

                                        14 -> StringSetting(
                                            title = "Shadow Color\n[Only used if \"Cast Shadow\" is true]",
                                            textFieldLabel = "AARRGGBB Only",
                                            defaultInputValue = "88676767",
                                            updateSetting = {
                                                applyColorOrDisplayWarning(it) { hex ->
                                                    selectedProfile.design.shadow.color = hex
                                                }
                                            },
                                        )

                                        15 -> ArrayLongSetting(
                                            title = "Shadow Corner Radius\nComma delimited in order:\nTopLeft, TopRight, BottomRight\n[Only used if \"Cast Shadow\" is true]",
                                            defaultInputValue = "8,8,8,8",
                                            updateSetting = {
                                                it.forEachIndexed { index, radius ->
                                                    when(index){
                                                        0 -> selectedProfile.design.shadow.cornerRadius.topLeft = radius.toFloat()
                                                        1 -> selectedProfile.design.shadow.cornerRadius.topRight = radius.toFloat()
                                                        2 -> selectedProfile.design.shadow.cornerRadius.bottomRight = radius.toFloat()
                                                        3 -> selectedProfile.design.shadow.cornerRadius.bottomLeft = radius.toFloat()
                                                    }
                                                }
                                            }
                                        )

                                        16 -> ArrayLongSetting(
                                            title = "Shadow Inset\nComma delimited in order:\nTopLeft,TopRight,BottomRight,BottomLeft\n[Only used if \"Cast Shadow\" is true]",
                                            defaultInputValue = "8,8,8,8",
                                            updateSetting = {
                                                it.forEachIndexed { index, inset ->
                                                    when(index){
                                                        0 -> selectedProfile.design.shadow.inset.left = inset.toInt()
                                                        1 -> selectedProfile.design.shadow.inset.top = inset.toInt()
                                                        2 -> selectedProfile.design.shadow.inset.right = inset.toInt()
                                                        3 -> selectedProfile.design.shadow.inset.bottom = inset.toInt()
                                                    }
                                                }
                                            }
                                        )

                                        17 -> ArrayLongSetting(
                                            title = "Shadow Padding\nComma delimited in order:\nLeft,Top,Right,Bottom\n[Only used if \"Cast Shadow\" is true]",
                                            defaultInputValue = "8,8,8,8",
                                            updateSetting = {
                                                it.forEachIndexed { index, padding ->
                                                    when(index){
                                                        0 -> selectedProfile.design.shadow.padding.left = padding.toInt()
                                                        1 -> selectedProfile.design.shadow.padding.top = padding.toInt()
                                                        2 -> selectedProfile.design.shadow.padding.right = padding.toInt()
                                                        3 -> selectedProfile.design.shadow.padding.bottom = padding.toInt()
                                                    }
                                                }
                                            }
                                        )

                                        18 -> DropdownSetting(
                                            title = "Position\nApplies to activities that have not called Whisper yet.\n[Applies to all profiles]",
                                            arr = arrayOf(
                                                "TOP_LEFT",
                                                "TOP_CENTER",
                                                "TOP_RIGHT",
                                                "TOP_START",
                                                "TOP_END",
                                                "TOP_FULL",
                                            ),
                                            defaultInputValue = Whisper.GlobalConfig.positionOnScreen.toString(),
                                            updateSetting = {
                                                Whisper.GlobalConfig.positionOnScreen = Whisper.Enums.PositionOnScreen.valueOf(it)
                                            }
                                        )

                                        19 -> DropdownSetting(
                                            title = "Sort Order\n[Applies to all profiles]",
                                            arr = arrayOf("BELOW", "ABOVE"),
                                            defaultInputValue = Whisper.GlobalConfig.sortOrder.toString(),
                                            updateSetting = { Whisper.GlobalConfig.sortOrder = Whisper.Enums.SortOrder.valueOf(it) }
                                        )

                                        20 -> LongSetting(
                                            title = "Spacing Between Visible Whispers\n[Applies to all profiles]",
                                            defaultInputValue = "12",
                                            updateSetting = { Whisper.GlobalConfig.displaySpace = it.toInt() }
                                        )

                                        21 -> LongSetting(
                                            title = "Offset X\n[Applies to all profiles]",
                                            defaultInputValue = "12",
                                            updateSetting = { Whisper.GlobalConfig.offset.x = it.toInt() }
                                        )

                                        22 -> LongSetting(
                                            title = "Offset Y\n[Applies to all profiles]",
                                            defaultInputValue = "12",
                                            updateSetting = { Whisper.GlobalConfig.offset.y = it.toInt() }
                                        )

                                        23 -> BooleanSetting(
                                            title = "Additional Offset for Status Bar\n[Applies to all profiles]",
                                            defaultInputValue = true,
                                            updateSetting = { Whisper.GlobalConfig.offset.additionalOffsetForStatusBar = it }
                                        )

                                        24 -> BooleanSetting(
                                            title = "Whisper Dismissible by Tap\n[Applies to all profiles]",
                                            defaultInputValue = true,
                                            updateSetting = { Whisper.GlobalConfig.tapToDismiss = it }
                                        )

                                        25 -> LongSetting(
                                            title = "Max Whispers Displayable at Once\n[Applies to all profiles]",
                                            defaultInputValue = "3",
                                            updateSetting = { Whisper.GlobalConfig.maxVisible = it.toInt() }
                                        )

                                        26 -> BooleanSetting(
                                            title = "Timeout Only for Oldest Whisper\n[Applies to all profiles]",
                                            defaultInputValue = true,
                                            updateSetting = { Whisper.GlobalConfig.timeoutOnlyForOldestWhisper = it }
                                        )

                                        27 -> LongSetting(
                                            title = "Timeout Length Per Character (ms)\n[Applies to all profiles]",
                                            defaultInputValue = "75",
                                            updateSetting = { Whisper.GlobalConfig.timeoutLengthPerCharacter = it }
                                        )

                                        28 -> LongSetting(
                                            title = "Transition Animation Duration (ms)\n[Applies to all profiles]",
                                            defaultInputValue = "400",
                                            updateSetting = { Whisper.GlobalConfig.animationTransitionDuration = it }
                                        )

                                        29 -> LongSetting(
                                            title = "Minimum Display Duration (ms)\n[Applies to all profiles]",
                                            defaultInputValue = "2200",
                                            updateSetting = { Whisper.GlobalConfig.durationDisplayMinimum = it }
                                        )

                                        30 -> LongSetting(
                                            title = "Maximum Display Duration (ms)\n[Applies to all profiles]",
                                            defaultInputValue = "22000",
                                            updateSetting = { Whisper.GlobalConfig.durationDisplayMaximum = it }
                                        )

                                        31 -> DropdownSetting(
                                            title = "Sound Trigger",
                                            arr = arrayOf(
                                                "NEVER",
                                                "CUSTOM_EVERY_WHISPER",
                                                "CUSTOM_SOLE_WHISPER",
                                                "DEVICE_EVERY_WHISPER",
                                                "DEVICE_SOLE_WHISPER"
                                            ),
                                            defaultInputValue = selectedProfile.sound.trigger.toString(),
                                            updateSetting = {
                                                selectedProfile.sound.trigger = Whisper.Enums.TriggerSound.valueOf(it)
                                            }
                                        )

                                        32 -> StringSetting(
                                            title = "Custom Sound\n[Only used if \"Sound Trigger\" is not \"NEVER\"]",
                                            textFieldLabel = "Sound file w/o extension",
                                            defaultInputValue = "horn",
                                            updateSetting = { selectedProfile.sound.customSound = it },
                                        )

                                        33 -> DropdownSetting(
                                            title = "Vibration Trigger",
                                            arr = arrayOf("NEVER", "EVERY_WHISPER", "SOLE_WHISPER"),
                                            defaultInputValue = selectedProfile.vibrate.trigger.toString(),
                                            updateSetting = {
                                                selectedProfile.vibrate.trigger = Whisper.Enums.TriggerVibrate.valueOf(it)
                                            }
                                        )

                                        34 -> ArrayLongSetting(
                                            title = "Vibration Pattern\n[Only used if \"Vibrate Trigger\" is not \"NEVER\"]",
                                            defaultInputValue = "0,100,50,100",
                                            updateSetting = { selectedProfile.vibrate.vibrationPattern = it }
                                        )

                                        35 -> DropdownSetting(
                                            title = "Pixel Density Unit\n[Applies to all profiles]",
                                            arr = arrayOf("DP", "PX"),
                                            defaultInputValue = Whisper.GlobalConfig.pixelDensityUnit.toString(),
                                            updateSetting = {
                                                Whisper.GlobalConfig.pixelDensityUnit = Whisper.Enums.PixelDensity.valueOf(it)
                                            }
                                        )

                                        36 -> DropdownSetting(
                                            title = "Background Gradient Orientation\n[Only used if \"Background color type\" is a gradient]",
                                            arr = arrayOf("TOP_BOTTOM", "RIGHT_LEFT", "BOTTOM_TOP", "LEFT_RIGHT", "TOPRIGHT_BOTTOMLEFT", "BOTTOMRIGHT_TOPLEFT", "BOTTOMLEFT_TOPRIGHT", "TOPLEFT_BOTTOMRIGHT"),
                                            defaultInputValue = selectedProfile.design.background.gradientOrientation.toString(),
                                            updateSetting = {
                                                selectedProfile.design.background.gradientOrientation =
                                                    Whisper.Enums.GradientOrientation.valueOf(it)
                                            }
                                        )

                                        37 -> FloatSetting(
                                            title = "Background Gradient Center X\n[Only used if \"Background Type\" is \"GRADIENT_RADIAL\" or \"GRADIENT_SWEEP\"]",
                                            defaultInputValue = "0.5",
                                            updateSetting = { selectedProfile.design.background.gradientCenterX = it }
                                        )

                                        38 -> FloatSetting(
                                            title = "Background Gradient Center Y\n[Only used if \"Background Type\" is \"GRADIENT_RADIAL\" or \"GRADIENT_SWEEP\"]",
                                            defaultInputValue = "0.5",
                                            updateSetting = { selectedProfile.design.background.gradientCenterY = it }
                                        )

                                        39 -> FloatSetting(
                                            title = "Background Gradient Radius\n[Only used if \"Background Type\" is \"GRADIENT_RADIAL\"]",
                                            defaultInputValue = "120.0",
                                            updateSetting = { selectedProfile.design.background.gradientRadius = it }
                                        )
                                    }
                                }
                            }

                            Line()

                            Whisper.Profiles.default.design.text.size = 23f

                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                onClick = {

                                    Whisper.critical(
                                        activity = this@MainActivity,
                                        message = "Reset all configurations and profiles back to their default settings (This will restart the app)? Tap to confirm. Otherwise this message will timeout and cancel after 15 seconds.",
                                        duration = 15000,
                                        onClick = { _, _ ->
                                            val ctx = applicationContext
                                            val pm = ctx.packageManager
                                            val intent =
                                                pm.getLaunchIntentForPackage(ctx.packageName)
                                            val mainIntent =
                                                Intent.makeRestartActivityTask(intent!!.component)
                                            ctx.startActivity(mainIntent)
                                            Runtime.getRuntime().exit(0)
                                        }
                                    )
                                }
                            ) {
                                Text(text = "Reset all settings")
                            }

                            Line()
                        }
                    }
                }
            }
        }
        Whisper.GlobalConfig.applyYamlIfNeverRan(this@MainActivity)
    }

    private fun isColorHexValid(hex: String): Boolean {
        return try {
            android.graphics.Color.parseColor("#$hex")
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun applyColorOrDisplayWarning(hex: String, colorSetting: (String) -> Unit) {
        val isValid = isColorHexValid(hex)

        if(isValid) {
            colorSetting(hex)
        } else {
            displaySelectedProfileWhisper("\"$hex\" is an invalid color. AARRGGBB hex values only for colors. Color was not applied.")
        }
    }

    @Composable
    private fun StringSetting(
        title: String,
        textFieldLabel: String,
        defaultInputValue: String,
        updateSetting: (String) -> Unit,
    ) {
        var inputText by rememberSaveable { mutableStateOf(defaultInputValue) }

        fun buttonClicked(updateSetting: (String) -> Unit) {
            updateSetting(inputText)
            displayChangeWhisper(title, inputText)
        }

        SettingTemplate(
            title = title,
            composableField = {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(textFieldLabel) }
                )
            },
            buttonClicked = {
                buttonClicked(updateSetting)
            }
        )
    }

    @Composable
    private fun ArrayLongSetting(
        title: String,
        defaultInputValue: String,
        updateSetting: (LongArray) -> Unit,
    ) {
        var inputText by rememberSaveable { mutableStateOf(defaultInputValue) }

        fun buttonClicked(updateSetting: (LongArray) -> Unit) {
            val arr = arrayListOf<Long>()
            inputText.split(",").forEach {
                val integer = it.toLongOrNull()
                if(integer == null) {
                    arr.clear()
                    inputText = "Comma delimited integers only"
                    return@forEach
                }
                arr.add(integer)
            }
            if(arr.isEmpty()) {
                inputText = "Comma delimited integers only"
                return
            }

            updateSetting(arr.toLongArray())

            displayChangeWhisper(title, inputText)
        }

        SettingTemplate(
            title = title,
            composableField = {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                )
            },
            buttonClicked = {
                buttonClicked(updateSetting)
            }
        )
    }

    @Composable
    private fun LongSetting(
        title: String,
        defaultInputValue: String,
        updateSetting: (Long) -> Unit,
    ) {
        var inputText by rememberSaveable { mutableStateOf(defaultInputValue) }

        fun buttonClicked(updateSetting: (Long) -> Unit) {
            inputText.toLongOrNull()?.let {
                updateSetting(it)
                displayChangeWhisper(title, inputText)
            } ?: run {
                inputText = "Integers Only"
            }
        }

        SettingTemplate(
            title = title,
            composableField = {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                )
            },
            buttonClicked = {
                buttonClicked(updateSetting)
            }
        )
    }

    @Composable
    private fun FloatSetting(
        title: String,
        defaultInputValue: String,
        updateSetting: (Float) -> Unit,
    ) {
        var inputText by rememberSaveable { mutableStateOf(defaultInputValue) }

        fun buttonClicked(updateSetting: (Float) -> Unit) {
            inputText.toFloatOrNull()?.let {
                updateSetting(it)
                displayChangeWhisper(title, inputText)
            } ?: run {
                inputText = "Decimals Only"
            }
        }

        SettingTemplate(
            title = title,
            composableField = {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                )
            },
            buttonClicked = {
                buttonClicked(updateSetting)
            }
        )
    }

    @Composable
    private fun DropdownSetting(
        title: String,
        arr: Array<String>,
        defaultInputValue: String,
        updateSetting: (String) -> Unit,
    ) {
        var choice by rememberSaveable { mutableStateOf(defaultInputValue) }

        fun buttonClicked(updateSetting: (String) -> Unit) {
            updateSetting(choice)
            displayChangeWhisper(title, choice)
        }

        SettingTemplate(
            title = title,
            composableField = {
                DropDownList(
                    listItems = arr,
                    onSelected = { index ->
                        choice = arr[index]
                    }
                )
            },
            buttonClicked = {
                buttonClicked(updateSetting)
            }
        )
    }

    @Composable
    private fun BooleanSetting(
        title: String,
        defaultInputValue: Boolean,
        updateSetting: (Boolean) -> Unit,
    ) {
        var choice by rememberSaveable { mutableStateOf(defaultInputValue) }

        fun buttonClicked(updateSetting: (Boolean) -> Unit) {
            updateSetting(choice)
            displayChangeWhisper(title, choice.toString())
        }

        val arr = arrayOf("true", "false")

        SettingTemplate(
            title = title,
            composableField = {
                DropDownList(
                    listItems = arr,
                    defaultIndex = arr.indexOf(defaultInputValue.toString()),
                    onSelected = { index ->
                        choice = arr[index].toBoolean()
                    }
                )
            },
            buttonClicked = {
                buttonClicked(updateSetting)
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropDownList(
        listItems: Array<String>,
        globalState: MutableState<String>? = null,
        defaultIndex: Int? = null,
        onSelected: (Int) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }
        val itemIndex = if(defaultIndex != null && defaultIndex >= 0 && defaultIndex < listItems.size) {
            defaultIndex
        } else {
            0
        }
        var selectedText by remember { globalState ?: mutableStateOf(listItems[itemIndex]) }

        Box(
            modifier = Modifier
                .wrapContentWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {
                TextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listItems.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                selectedText = item
                                expanded = false
                                onSelected(index)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SettingTemplate(
        title: String,
        composableField: @Composable () -> Unit,
        buttonClicked: () -> Unit,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(20.dp))
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 2.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(15.dp)
                    )
                    .background(
                        Color(0x66666666),
                        shape = RoundedCornerShape(15.dp)
                    )
        ) {

            Spacer(modifier = Modifier.height(8.dp))
            SectionSubtitleCentered(text = title)
            Spacer(modifier = Modifier.height(8.dp))
            Line()
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                composableField()
            }
            Row {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    onClick = {
                        buttonClicked()
                    }
                ) {
                    Text("Apply / Demo")
                }
            }
        }
    }

    @Composable
    private fun CallWhisper(buttonText: String, action: () -> Unit) {
        Button(
            modifier = Modifier.padding(4.dp),
            onClick = {
                action()
            }
        ) {
            Text(text = buttonText)
        }
    }

    @Composable
    private fun ConfigDDL() {
        DropDownList(
            listItems = arrayOfProfiles,
            globalState = selectedProfileStr,
            onSelected = { index ->
                selectedProfile = when(arrayOfProfiles[index]) {
                    PROFILE_DEFAULT -> Whisper.Profiles.default
                    PROFILE_INFO -> Whisper.Profiles.info
                    PROFILE_ERROR -> Whisper.Profiles.error
                    PROFILE_WARN -> Whisper.Profiles.warn
                    PROFILE_FATAL -> Whisper.Profiles.fatal
                    PROFILE_CRITICAL -> Whisper.Profiles.critical
                    PROFILE_TRACE -> Whisper.Profiles.trace
                    PROFILE_DEBUG -> Whisper.Profiles.debug
                    else -> Whisper.Profiles.default
                }
            }
        )
    }

    private fun displayChangeWhisper(title: String, change: String) {
        val message = when(selectedProfileStr.value) {
            PROFILE_DEFAULT -> "Applied $change $title with the default profile settings"
            PROFILE_INFO -> "Applied $change $title with the info profile settings"
            PROFILE_ERROR -> "Applied $change $title with the error profile settings"
            PROFILE_WARN -> "Applied $change $title with the warn profile settings"
            PROFILE_FATAL -> "Applied $change $title with the fatal profile settings"
            PROFILE_CRITICAL -> "Applied $change $title with critical profile settings"
            PROFILE_TRACE -> "Applied $change $title with Default trace settings"
            PROFILE_DEBUG -> "Applied $change $title with Default debug settings"
            else -> "Applied $change $title with Default profile settings"
        }
        displaySelectedProfileWhisper(message)
    }

    private fun displaySelectedProfileWhisper(message: String) {
        when(selectedProfileStr.value) {
            PROFILE_DEFAULT -> Whisper.default(this@MainActivity, message)
            PROFILE_INFO -> Whisper.info(this@MainActivity, message)
            PROFILE_ERROR -> Whisper.error(this@MainActivity, message)
            PROFILE_WARN -> Whisper.warn(this@MainActivity, message)
            PROFILE_FATAL -> Whisper.fatal(this@MainActivity, message)
            PROFILE_CRITICAL -> Whisper.critical(this@MainActivity, message)
            PROFILE_TRACE -> Whisper.trace(this@MainActivity, message)
            PROFILE_DEBUG -> Whisper.debug(this@MainActivity, message)
            else -> Whisper.default(this@MainActivity, message)
        }
    }

    companion object {
        const val DARK_THEME_KEY = "darkTheme"
        const val SIMPLE_VIEW_MESSAGE_KEY = "messageKey"
    }
}

@Composable
fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = textModifier
    )
}

@Composable
fun SectionSubtitle(text: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = textModifier
    )
}

@Composable
fun SectionSubtitleCentered(text: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = textModifier,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun Line() {
    Divider(
        color = DividerDefaults.color,
        modifier = Modifier
            .fillMaxWidth()
            .width(1.dp)
    )
}