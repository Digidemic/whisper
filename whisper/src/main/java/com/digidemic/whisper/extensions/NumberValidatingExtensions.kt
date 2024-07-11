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

/** If the Float is 0 or greater, return it. Otherwise return the [defaultValue] passed. */
internal fun Float.zeroOrGreater(defaultValue: Float) = if(this >= 0) this else defaultValue

/** If the Float is greater than 0, return it. Otherwise return the [defaultValue] passed. */
internal fun Float.greaterThanZero(defaultValue: Float) = if(this > 0) this else defaultValue

/** If the Long is 0 or greater, return it. Otherwise return the [defaultValue] passed. */
internal fun Long.zeroOrGreater(defaultValue: Long) = if(this >= 0) this else defaultValue

/** If the Long is greater than 0, return it. Otherwise return the [defaultValue] passed. */
internal fun Long.greaterThanZero(defaultValue: Long) = if(this > 0) this else defaultValue

/** If the Int is 0 or greater, return it. Otherwise return the [defaultValue] passed. */
internal fun Int.zeroOrGreater(defaultValue: Int) = if(this >= 0) this else defaultValue

/** If the Int is greater than 0, return it. Otherwise return the [defaultValue] passed. */
internal fun Int.greaterThanZero(defaultValue: Int) = if(this > 0) this else defaultValue