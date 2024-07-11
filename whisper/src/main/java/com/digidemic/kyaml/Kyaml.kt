/**
 * Kyaml v1.1.0 - https://github.com/Digidemic/kyaml
 * (c) 2024 DIGIDEMIC, LLC - All Rights Reserved
 * Kyaml developed by Adam Steinberg of DIGIDEMIC, LLC
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

package com.digidemic.kyaml

import android.app.Activity
import androidx.annotation.RawRes
import androidx.annotation.VisibleForTesting
import java.io.BufferedReader

/**
 * Kyaml is a simple, flexible, and forgiving YAML parser for Android.
 *
 * Pass a YAML file from `/assets/` or `/res/raw/` to return parsed key/values for each item.
 * Each value returned will be casted as the type found.
 *   Examples of value types returned from Kyaml:
 *     - key1: test! = `String`
 *     - key2: 123 = `Int`
 *     - key3: 2147483648 = `Long`
 *     - key4: [ 1, 2, 3 ] = `List of Integers`
 */
internal class Kyaml {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val activeSequenceList = mutableListOf<String>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val indentation = mutableListOf<Indentation>()

    private val activity: Activity
    private val onEachItem: (String, Any?) -> Unit
    private val onError: (Exception) -> Unit
    private val activeBlockScalarList = mutableListOf<String>()

    private var gatheringLiteralBlockScalar = false
    private var gatheringFoldedBlockScalar = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal data class SequenceType(
        val elementType: ValueType,
        val containsNull: Boolean,
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal data class Indentation(
        val key: String,
        val prefixedSpacesForKey: Int,
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal enum class ValueType {
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN,
        STRING,
        NULL,
    }

    /**
     * Specific constructor exists only for unit testing.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal constructor(activity: Activity) {
        this.activity = activity
        this.onEachItem = { _, _ -> }
        this.onError = {}
    }

    /**
     * One of two public constructor options.
     * Specific constructor requires the YAML file name from the assets directory.
     *
     * @param activity active Activity.
     * @param yamlFileNameInAssets YAML file name and extension located in `/assets/` to consume.
     * @param onEachItem Called on every item parsed from the YAML file. Included will be the key and value. The key is always of type `String`. The value will be of the detected type when parsed. This may include `Int`, `Long`, `Float`, `Double`, `Boolean`, `String`, or `null`. The value may also be a `List` of any of these types.
     * @param onError optional, Any exception caught within Kyaml will stop the process and pass the exception here. An `IllegalArgumentException` will be thrown if `yamlFileNameInAssets` contains a file name that could not be found. It is not known if any other exception will be thrown when using Kyaml.
     */
    constructor(
        activity: Activity,
        yamlFileNameInAssets: String,
        onEachItem: (String, Any?) -> Unit,
        onError: (Exception) -> Unit = {},
    ) {
        this.activity = activity
        this.onEachItem = onEachItem
        this.onError = onError
        readThenParseYamlFile(yamlFileNameInAssets, null)
    }

    /**
     * One of two public constructor options.
     * Specific constructor requires the YAML resource id (int) from the raw directory.
     *
     * @param activity active Activity.
     * @param yamlResourceIdInRaw YAML file located in `/res/raw/` to consume.
     * @param onEachItem Called on every item parsed from the YAML file. Included will be the key and value. The key is always of type `String`. The value will be of the detected type when parsed. This may include `Int`, `Long`, `Float`, `Double`, `Boolean`, `String`, or `null`. The value may also be a `List` of any of these types.
     * @param onError optional, Any exception caught within Kyaml will stop the process and pass the exception here. An `IllegalArgumentException` will be thrown if `yamlFileNameInAssets` contains a file name that could not be found. It is not known if any other exception will be thrown when using Kyaml.
     */
    constructor(
        activity: Activity,
        @RawRes yamlResourceIdInRaw: Int,
        onEachItem: (String, Any?) -> Unit,
        onError: (Exception) -> Unit = {},
    ) {
        this.activity = activity
        this.onEachItem = onEachItem
        this.onError = onError
        readThenParseYamlFile( null, yamlResourceIdInRaw)
    }

    /**
     * Read YAML file from /assets/ or /res/raw/ depending on what is passed.
     * Upon read success, pass YAML contents (in a List<String>) to [parseYamlList].
     * Upon read failure, pass [IllegalArgumentException] to [onError].
     */
    private fun readThenParseYamlFile(
        yamlFileNameInAssets: String?,
        @RawRes yamlResourceIdInRaw: Int?,
    ) {
        var yamlList: List<String>? = null
        if(yamlFileNameInAssets != null) {
            yamlList = getYamlListFromAssets(activity, yamlFileNameInAssets)
        } else if(yamlResourceIdInRaw != null) {
            try {
                yamlList = activity.resources
                    .openRawResource(yamlResourceIdInRaw)
                    .bufferedReader()
                    .use(BufferedReader::readLines)
            } catch (_: Exception) { }
        }

        if(yamlList != null) {
            try {
                parseYamlList(
                    startingIndex = 0,
                    yamlList = yamlList.toMutableList()
                )
            } catch (exception: Exception) {
                onError(exception)
            }
        } else {
            onError(
                IllegalArgumentException(
                    "Could not open ${yamlFileNameInAssets ?: yamlResourceIdInRaw}"
                )
            )
        }
    }

    /**
     * Tries to load the passed YAML file name.
     * If an extension is not part of the passed file name, it will try to be opened with a list of acceptable file
     * extensions.
     * Ex:
     *   Given for all examples: file exists in /assets/testFile.yaml
     *     1) fileName="testFile.yaml" -> listOf(...)
     *     2) fileName="testFile" -> listOf(...)
     *     3) fileName="testFile.txt" -> null
     *     4) fileName="invalid.yml" -> null
     */
    private fun getYamlListFromAssets(
        activity: Activity,
        fileName: String,
    ): List<String>? {
        var yamlList: List<String>? = null
        for(i in acceptedFileExtensions.indices) {
            try {
                yamlList = activity
                    .assets
                    .open("$fileName${acceptedFileExtensions[i]}")
                    .bufferedReader()
                    .use(BufferedReader::readLines)
                break
            } catch (_: Exception) { }
        }
        return yamlList
    }

    /**
     * Main function. Takes YAML content loaded as a List<String> and loops through/performing actions on each element.
     */
    private fun parseYamlList(
        startingIndex: Int,
        yamlList: MutableList<String>
    ) {
        // Loop each element of YAML list.
        // List may dynamically change and when it does, loop will terminate then
        // restart with a new starting index to properly adjust with this change.
        for(lineIndex in startingIndex until yamlList.size) {
            val currentLine = yamlList[lineIndex]

            // Split key and value: "  fontSize: 30" -> ["  fontSize", " 30"]
            val keyValue = currentLine.split(KEY_VALUE_SPLIT_DELIMITER)

            // get indent spaces: ["  fontSize", "30"] -> 2
            val prefixedSpacesInLine = getPrefixedSpacesCount(keyValue.first())

            // Isolate key without indentation as its own variable: ["  fontSize", "30"] -> "fontSize"
            val key = keyValue.first().trim()

            // Check if have been building a sequence line by line and finished with previous line (current line does not add to sequence).
            if (indentation.isNotEmpty() && activeSequenceList.isNotEmpty() && !key.startsWith(SEQUENCE_PREFIX)) {
                applyGatheredSequenceValues()

                // Check if have been building a block scalar (either literal or folded).
            } else if (gatheringLiteralBlockScalar || gatheringFoldedBlockScalar) {
                // If indentation has not changed, current line is part of the active block scalar.
                // Otherwise gathering block scalar has completed with previous line.
                if(indentation.isNotEmpty() && prefixedSpacesInLine > indentation.last().prefixedSpacesForKey) {
                    activeBlockScalarList.add(key)
                } else {
                    applyBlockScalarLines()
                }
            }

            // If was able to split out key from current line (otherwise line may be a special character or sequence).
            if (keyValue.size >= 2) {

                // Assemble value if broken up from prior key/value split: ["  30", " 40 ", "50  "] -> "30: 40 :50".
                val value = getValueFromSeparatedKeyPair(keyValue, KEY_VALUE_SPLIT_DELIMITER)

                // If key is valid (key's first character is lower a-z or upper A-Z).
                if (isKeyValid(key)) {

                    // Update the collection of nesting indentations depending on current line's indentation.
                    adjustIndentation(prefixedSpacesInLine)

                    // When current value indicates key needs to be added as a new indentation.
                    // This includes when value is empty or value indicates start of block scalar.
                    // In addition, if value indicates start of block scalar, set proper flag to true.
                    if (value.isEmpty() || value == LITERAL_BLOCK_SCALAR || value == FOLDED_BLOCK_SCALAR) {
                        indentation.add(Indentation(key, prefixedSpacesInLine))
                        when (value) {
                            LITERAL_BLOCK_SCALAR -> gatheringLiteralBlockScalar = true
                            FOLDED_BLOCK_SCALAR -> gatheringFoldedBlockScalar = true
                        }

                        // Current line is a flow sequence or dictionary
                    } else if (
                        value.isSurroundedBy(OPEN_SQUARE_BRACKET, CLOSED_SQUARE_BRACKET)
                        || value.isSurroundedBy(OPEN_CURLY_BRACE, CLOSED_CURLY_BRACE)
                    ) {

                        // Add key part of indentation.
                        val newSpaces = List(prefixedSpacesInLine) { SPACE }.joinToString(separator = "")
                        indentation.add(Indentation(key, newSpaces.length))

                        // Current line is a flow sequence
                        if(value.isSurroundedBy(OPEN_SQUARE_BRACKET, CLOSED_SQUARE_BRACKET)) {
                            sequenceAction(
                                flowCollectionToList(value, OPEN_SQUARE_BRACKET, CLOSED_SQUARE_BRACKET)
                            )

                            // Current line is a flow dictionary
                        } else if (value.isSurroundedBy(OPEN_CURLY_BRACE, CLOSED_CURLY_BRACE)) {
                            // Add each key value pair to existing YAML list.
                            //  Ex: "foo: { bar: 1, baz: 2 }"
                            //    ->
                            //    bar: 1
                            //    baz: 2
                            flowCollectionToList(value, OPEN_CURLY_BRACE, CLOSED_CURLY_BRACE)
                                .forEachIndexed { index, item ->
                                    yamlList.add(lineIndex + 1 + index, "$newSpaces $item")
                                }

                            // Update current line to only be the key.
                            // Ex: "foo: { bar: 1, baz: 2 }" -> "foo:"
                            yamlList[lineIndex] = yamlList[lineIndex]
                                .split(KEY_VALUE_SPLIT_DELIMITER)
                                .first()
                                .plus(KEY_VALUE_SPLIT_DELIMITER)

                            // YAML list has been updated. Reset loop starting from next line.
                            parseYamlList(lineIndex + 1, yamlList)
                            return
                        }

                        // No special action required for current line. Pass key and value to [itemAction].
                    } else {
                        itemAction(key, value)
                    }
                }

                // Current line is an element of a sequence. Add to sequence list.
            } else if (key.startsWith(SEQUENCE_PREFIX)) {
                val arrayKeyValue = key.split(SEQUENCE_PREFIX)
                val value = getValueFromSeparatedKeyPair(arrayKeyValue, SEQUENCE_PREFIX)
                activeSequenceList.add(value)

                // Current line is a special string, send to [onEachItem].
            } else if (prefixedSpacesInLine == 0 && (key == MULTI_DOCUMENT_START_INDICATOR || key == MULTI_DOCUMENT_END_INDICATOR)) {
                onEachItem(key, null)
            }
        }

        // Loop complete. Apply any active multiline values being parsed before loop ended that did not complete.
        if(indentation.isNotEmpty() && activeSequenceList.isNotEmpty()) {
            applyGatheredSequenceValues()
        }
        if(gatheringLiteralBlockScalar || gatheringFoldedBlockScalar) {
            applyBlockScalarLines()
        }
    }

    /**
     * Take string value and attempts to to find and cast to its proper type.
     * Passes this value with its key to [onEachItem].
     * Ex:
     *   1) "1" -> 1 (Int)
     *   2) "1.1" -> 1.1 (Float)
     *   3) "TruE" -> true (Boolean)
     *   4) "\"one\"" -> "one"
     */
    private fun itemAction(
        key: String,
        value: String
    ) {
        when(getValueType(value)) {
            ValueType.INTEGER -> onEachItem(getKeyWithNesting(key), value.toIntOrNull() ?: adjustValueQuotes(value))
            ValueType.LONG -> onEachItem(getKeyWithNesting(key), value.toLongOrNull() ?: adjustValueQuotes(value))
            ValueType.FLOAT -> onEachItem(getKeyWithNesting(key), value.toFloatOrNull() ?: adjustValueQuotes(value))
            ValueType.DOUBLE -> onEachItem(getKeyWithNesting(key), value.toDoubleOrNull() ?: adjustValueQuotes(value))
            ValueType.BOOLEAN -> onEachItem(getKeyWithNesting(key), value.toBooleanStrictOrNull() ?: adjustValueQuotes(value))
            ValueType.NULL -> onEachItem(getKeyWithNesting(key), null)
            ValueType.STRING -> onEachItem(getKeyWithNesting(key), adjustValueQuotes(value))
        }
    }

    /**
     * Take lists of strings and attempts to convert the type based on the elements in the list.
     * Passes this list with its key to [onEachItem].
     * Ex:
     *   1) List<String>("1", "2", "3") -> List<Int>(1, 2, 3)
     *   2) List<String>("1.1", "2.2", "3.3") -> List<Float>(1.1, 2.2, 3.3)
     *   3) List<String>("1", "2.22", "3") -> List<Float>(1.0, 2.22, 3.0)
     *   4) List<String>("true", "false", "TrUe", "FaLSe") -> List<Boolean>(true, false, true, false)
     *   5) List<String>("one", "\"two\"", "'three'") -> List<String>("one", "two", "three")
     *   6) List<String>("null", "two", "three") -> List<String?>(null, "two", "three")
     *   7) List<String>("1", "two", "true") -> List<String>("1", "two", "true")
     */
    private fun sequenceAction(
        lst: List<String>,
    ) {
        val constructedKey = getKeyWithNesting()
        val sequenceType = getSequenceType(lst)
        when(sequenceType.elementType) {
            ValueType.INTEGER -> onEachItem(
                constructedKey,
                when(sequenceType.containsNull) {
                    true -> lst.map { it.toIntOrNull() }
                    false -> lst.map { it.toInt() }
                }
            )
            ValueType.LONG -> onEachItem(
                constructedKey,
                when(sequenceType.containsNull) {
                    true -> lst.map { it.toLongOrNull() }
                    false -> lst.map { it.toLong() }
                }
            )
            ValueType.FLOAT -> onEachItem(
                constructedKey,
                when(sequenceType.containsNull) {
                    true -> lst.map { it.toFloatOrNull() }
                    false -> lst.map { it.toFloat() }
                }
            )
            ValueType.DOUBLE -> onEachItem(
                constructedKey,
                when(sequenceType.containsNull) {
                    true -> lst.map { it.toDoubleOrNull() }
                    false -> lst.map { it.toDouble() }
                }
            )
            ValueType.BOOLEAN -> onEachItem(
                constructedKey,
                when(sequenceType.containsNull) {
                    true -> lst.map { it.lowercase().toBooleanStrictOrNull() }
                    false -> lst.map { it.lowercase().toBooleanStrict() }
                }
            )
            ValueType.STRING, ValueType.NULL -> onEachItem(
                constructedKey,
                when(sequenceType.containsNull) {
                    true -> {
                        lst.map {
                            when(it.equals(NULL_STR, ignoreCase = true)){
                                true -> null
                                false -> adjustValueQuotes(it)
                            }
                        }
                    }
                    false -> lst.map { adjustValueQuotes(it) }
                }
            )
        }
    }

    /**
     * Should be called after finished gathering a block scalar from YAML.
     * Function will convert scalar list to a string,
     * Send the key and string value to [onEachItem],
     * remove key from indentation list,
     * clear the [activeBlockScalarList],
     * set literal and folded scalar flags to false.
     *
     * Ex of events:
     *   activeBlockScalarList: ["This is", "a multiline", "String example."] -> onEachItem(key: "obj.scalarKey", value: "This is a multiline String example.")
     *   indentations: [("obj", 2), ("scalarKey", 4)] -> [("obj", 2)]
     *   activeBlockScalarList: ["This is", "a multiline", "String example."] -> []
     *   gatheringLiteralBlockScalar = false, gatheringFoldedBlockScalar = true -> gatheringLiteralBlockScalar = false, gatheringFoldedBlockScalar = false
     */
    private fun applyBlockScalarLines() {
        val blockScalarStringBuilder = StringBuilder()
        activeBlockScalarList.forEachIndexed { index, str ->
            blockScalarStringBuilder.append(str)
            if(index < activeBlockScalarList.size - 1) {
                blockScalarStringBuilder.append(
                    if(gatheringLiteralBlockScalar) LINE_BREAK else SPACE
                )
            }
        }
        onEachItem(
            getKeyWithNesting(),
            blockScalarStringBuilder.toString()
        )
        indentation.removeLast()
        activeBlockScalarList.clear()
        gatheringLiteralBlockScalar = false
        gatheringFoldedBlockScalar = false
    }

    /**
     * If String starts and ends with specific characters.
     * Ex:
     *   1) "[This is an example.]", startsWith = "[", endsWith "]" -> true
     *   2) "{This is an example.}", startsWith = "[", endsWith "]" -> false
     *   3) "[This is an example.}", startsWith = "[", endsWith "]" -> false
     */
    private fun String.isSurroundedBy(
        startsWith: String,
        endsWith: String
    ): Boolean = this.startsWith(startsWith) && this.endsWith(endsWith)

    /**
     * If string starts and ends with at least one of the passed in character.
     * Ex:
     *   1) "'This is an example.'", startsAndEndsWith = [!, '] -> true
     *   2) "'This is an example.'", startsAndEndsWith = ['] -> true
     *   3) "'This is an example.'", startsAndEndsWith = [!] -> false
     */
    @Suppress("SameParameterValue")
    private fun String.startsAndEndsWith(vararg startsAndEndsWith: String): Boolean {
        startsAndEndsWith.forEach {
            if(this.isSurroundedBy(it, it)) {
                return true
            }
        }
        return false
    }

    /**
     * If YAML key of item is a valid key.
     * Ex:
     *   1) "test123" -> true
     *   2) "TEST" -> true
     *   3) "123Test" -> false
     *   4) "_Test" -> false
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun isKeyValid(key: String) = Regex(VALID_KEY_PATTERN).matches(key)

    /**
     * Removes single or double quotes surrounding a string.
     * Ex:
     *   1) " \"  two spaces before and after  \" " -> "  two spaces before and after  "
     *   2) "'don't'" -> "don't"
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun adjustValueQuotes(
        value: String
    ) = value.trim().let {
        if(it.startsAndEndsWith(SINGLE_QUOTE, DOUBLE_QUOTE)) {
            return@let it.drop(1).dropLast(1)
        }
        return@let it
    }

    /**
     * Adjust indentation as needed for current key indentation.
     * Depending on current line spacing, determine what/if any objects within can be removed.
     * Ex:
     *   Given for all examples: [("a", 2), ("b", 4), ("c", 6)].
     *     1) If preSpaces=2 -> [].
     *     2) If preSpaces=3 -> [("a", 2)].
     *     3) If preSpaces=9 -> [("a", 2), ("b", 4), ("c", 6)]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun adjustIndentation(prefixedSpacesInLine: Int) {
        val indexToSublist = indentation
            .map { it.prefixedSpacesForKey }
            .indexOfFirst { prefixedSpacesInLine <= it }
        if(indexToSublist >= 0) {
            indentation.subList(indexToSublist, indentation.size).clear()
        }
    }

    /**
     * Takes lists of strings and attempts to find the proper type based on the elements in the list.
     * Ex:
     *   1) List<String>("1", "2", "3") -> SequenceType(ValueType.INTEGER, false)
     *   2) List<String>("1.1", "2.2", "3.3") -> SequenceType(ValueType.FLOAT, false)
     *   3) List<String>("1", "2.22", "3") -> SequenceType(ValueType.FLOAT, false)
     *   4) List<String>("true", "false", "TrUe", "FaLSe") -> SequenceType(ValueType.BOOLEAN, false)
     *   5) List<String>("one", "\"two\"", "'three'") -> SequenceType(ValueType.STRING, false)
     *   6) List<String>("null", "two", "three") -> SequenceType(ValueType.STRING, true)
     *   7) List<String>("1", "two", "true") -> SequenceType(ValueType.STRING, false)
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Suppress("KotlinConstantConditions")
    internal fun getSequenceType(list: List<String>): SequenceType {
        var sequenceContainsNulls = false
        var elementType: ValueType? = null
        for (currElement in list) {
            val currValueType = getValueType(currElement)
            // If type currently has been found as a String, no need to continue
            // searching alternative types. Only check for null elements.
            if(elementType == ValueType.STRING) {
                if(currValueType == ValueType.NULL) {
                    sequenceContainsNulls = true
                    break
                }
            } else {
                when (currValueType) {
                    // String is the default type if there is an element mismatch.
                    // If String, no need to continue searching alternative types.
                    ValueType.STRING -> {
                        elementType = ValueType.STRING
                        continue
                    }

                    ValueType.INTEGER -> when (elementType) {
                        ValueType.INTEGER,
                        ValueType.LONG,
                        ValueType.FLOAT,
                        ValueType.DOUBLE ->
                            continue
                        ValueType.BOOLEAN,
                        ValueType.STRING -> {
                            elementType = ValueType.STRING
                            continue
                        }
                        ValueType.NULL, null ->
                            elementType = ValueType.INTEGER
                    }

                    ValueType.LONG -> when (elementType) {
                        ValueType.LONG,
                        ValueType.DOUBLE ->
                            continue
                        ValueType.FLOAT,
                        ValueType.BOOLEAN,
                        ValueType.STRING -> {
                            elementType = ValueType.STRING
                            continue
                        }
                        ValueType.INTEGER,
                        ValueType.NULL,
                        null ->
                            elementType = ValueType.LONG
                    }

                    ValueType.FLOAT -> when (elementType) {
                        ValueType.LONG,
                        ValueType.BOOLEAN,
                        ValueType.STRING -> {
                            elementType = ValueType.STRING
                            continue
                        }
                        ValueType.FLOAT,
                        ValueType.DOUBLE ->
                            continue
                        ValueType.INTEGER,
                        ValueType.NULL,
                        null ->
                            elementType = ValueType.FLOAT
                    }

                    ValueType.DOUBLE -> when (elementType) {
                        ValueType.INTEGER,
                        ValueType.LONG,
                        ValueType.FLOAT,
                        ValueType.NULL,
                        null ->
                            elementType = ValueType.DOUBLE
                        ValueType.DOUBLE ->
                            continue
                        ValueType.BOOLEAN,
                        ValueType.STRING -> {
                            elementType = ValueType.STRING
                            continue
                        }
                    }

                    ValueType.BOOLEAN -> when (elementType) {
                        ValueType.INTEGER,
                        ValueType.LONG,
                        ValueType.FLOAT,
                        ValueType.DOUBLE,
                        ValueType.STRING -> {
                            elementType = ValueType.STRING
                            continue
                        }
                        ValueType.BOOLEAN ->
                            continue
                        ValueType.NULL, null ->
                            elementType = ValueType.BOOLEAN
                    }

                    ValueType.NULL ->
                        sequenceContainsNulls = true
                }
            }
        }
        return SequenceType(
            elementType = elementType ?: ValueType.STRING,
            containsNull = sequenceContainsNulls,
        )
    }

    /**
     * Takes a stringified sequence or map and converts it into a list.
     * Ex:
     *   1) startsWith="[", startsWith="]", value="[ foo, bar, baz  ]" -> List<String>("foo", "bar", "baz")
     *   2) startsWith="{", startsWith="}", value="{ foo: 1, bar: 2, baz: 3 }" -> List<String>("foo: 1", "bar: 2", "baz: 3")
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun flowCollectionToList(
        value: String,
        startsWith: String,
        endsWith: String
    ): List<String> =
        value
            .removeSurrounding(startsWith, endsWith)
            .split(COMMA_CHARACTER)
            .map { it.trim() }

    /**
     * Takes the string value and attempts to find the proper type of the value.
     * Ex:
     *   1) "1" -> ValueType.INTEGER
     *   2) "1.1" -> ValueType.FLOAT
     *   3) "TruE" -> ValueType.BOOLEAN
     *   4) "\"one\"" -> ValueType.STRING
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getValueType(value: String): ValueType {
        with(value.trim()) {
            if(startsAndEndsWith(SINGLE_QUOTE, DOUBLE_QUOTE)) {
                return ValueType.STRING
            } else if(equals(NULL_STR, ignoreCase = true) || this == NULL_CHARACTER) {
                return ValueType.NULL
            }
            if(contains(PERIOD_CHARACTER)) {
                toFloatOrNull()?.let {
                    if(it.isFinite()) {
                        return ValueType.FLOAT
                    }
                }
                toDoubleOrNull()?.let {
                    if(it.isFinite()) {
                        return ValueType.DOUBLE
                    }
                }
            }
            toIntOrNull()?.let {
                return ValueType.INTEGER
            }
            toLongOrNull()?.let {
                return ValueType.LONG
            }
            lowercase().toBooleanStrictOrNull()?.let {
                return ValueType.BOOLEAN
            }

            return ValueType.STRING
        }
    }

    /**
     * Should be called after finished gathering a sequence from YAML.
     * Function will call subsequent functions to assign a type to the list,
     * Send the list to [onEachItem],
     * remove key from indentation list,
     * then clear the [activeSequenceList]
     *
     * Ex of events:
     *   "time.intervals": ["15", "30", "45"] -> onEachItem(key: "time.intervals", value: [15, 30, 45]
     *   indentations: [("time", 2), ("intervals", 4)] -> [("time", 2)]
     *   activeSequenceList: ["15", "30", "45"] -> []
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun applyGatheredSequenceValues() {
        sequenceAction(activeSequenceList.toList())

        indentation.removeLastOrNull()
        activeSequenceList.clear()
    }

    /**
     * Take a string and get the count of spaces that the string begins with.
     * Ex: "  food: bread, apples, cheese" -> 2
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getPrefixedSpacesCount(
        str: String,
    ): Int {
        val prefixedSpaces = str.map { it.isWhitespace() }.indexOfFirst { !it }
        if(prefixedSpaces > 0) {
            return prefixedSpaces
        }
        return 0
    }

    /**
     * Take all values previously broken up including the key to return a string of just the value.
     * Because splitting the keypair with the original split character (like :), That character may
     * be valid in the value and should be re-added.
     * If a comment exists in the value, remove the comment too.
     * Ex:
     *   - Original KeyPair String:
     *   "colors: Primary: Red, Yellow, Blue # Not including secondary"
     *
     *   - Split by ":" to separate the key and value (key will always be first element):
     *   ["colors", "Primary", "Red, Yellow, Blue # Not including secondary"]
     *
     *   - Calling getValueFromSeparatedKeyPair with this list and ":" as joinDelimiter returns:
     *   "Primary: Red, Yellow, Blue"
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getValueFromSeparatedKeyPair(
        keyValue: List<String>,
        joinDelimiter: String,
    ) = keyValue
        .drop(1)
        .joinToString(joinDelimiter)
        .substringBefore(COMMENT_CHARACTER)
        .trim()

    /**
     * Create the key name applying any nesting.
     * Ex: indentation = [("a", 2), ("b", 4)], key = "c" -> "a.b.c"
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun getKeyWithNesting(
        key: String? = null,
    ) = indentation.joinToString(PERIOD_CHARACTER) {
        // Ex:  [("a", 2), ("b", 4)] -> "a.b"
        it.key
    }.let { nesting ->
        // key may not be passed in cases like block sequences.
        // The key name is already part of the indentation list.
        if(key != null) {
            if (nesting.isNotEmpty()) {
                "$nesting$PERIOD_CHARACTER$key"
            } else {
                key
            }
        } else {
            nesting
        }
    }

    private companion object {
        private val acceptedFileExtensions = arrayOf("", ".yaml", ".yml", ".YAML", ".YML")
        private const val PERIOD_CHARACTER = "."
        private const val OPEN_SQUARE_BRACKET = "["
        private const val CLOSED_SQUARE_BRACKET = "]"
        private const val OPEN_CURLY_BRACE = "{"
        private const val CLOSED_CURLY_BRACE = "}"
        private const val LITERAL_BLOCK_SCALAR = "|"
        private const val FOLDED_BLOCK_SCALAR = ">"
        private const val COMMENT_CHARACTER = "#"
        private const val COMMA_CHARACTER = ","
        private const val SEQUENCE_PREFIX = "-"
        private const val NULL_CHARACTER = "~"
        private const val KEY_VALUE_SPLIT_DELIMITER = ":"
        private const val SINGLE_QUOTE = "'"
        private const val DOUBLE_QUOTE = "\""
        private const val SPACE = " "
        private const val NULL_STR = "null"
        private const val LINE_BREAK = "\n"
        private const val MULTI_DOCUMENT_START_INDICATOR = "---"
        private const val MULTI_DOCUMENT_END_INDICATOR = "..."

        // Only if key's first character is lower a-z or upper A-Z
        private const val VALID_KEY_PATTERN = "^[a-zA-Z].*"
    }
}