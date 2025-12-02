package com.android.sample.data.local.utils

/**
 * This file contains helper functions to serialize and deserialize complex data types (like
 * List<String>, Set<String>, Map<String, Boolean>) into simple string formats suitable for storage
 * in databases that do not support these types natively.
 */

/** Helper to encode any List<String> into a single string. */
fun encodeList(list: List<String>): String = list.joinToString(",")

/** Helper to decode a List<String> from a stored string. */
fun decodeList(raw: String): List<String> = if (raw.isBlank()) emptyList() else raw.split(",")

/** Helper to encode a Set<String>. */
fun encodeSet(set: Set<String>): String = set.joinToString(",")

/** Helper to decode a Set<String>. */
fun decodeSet(raw: String): Set<String> = if (raw.isBlank()) emptySet() else raw.split(",").toSet()

/** Encode a Map<String, T> as a string, using [valueToString] to convert each value to String. */
fun <T> encodeMap(map: Map<String, T>, valueToString: (T) -> String): String =
    map.entries.joinToString(",") { "${it.key}:${valueToString(it.value)}" }

/**
 * Decode a Map<String, T> from a string, using [stringToValue] to convert each value from String.
 */
fun <T> decodeMap(raw: String, stringToValue: (String) -> T?): Map<String, T> =
    if (raw.isBlank()) emptyMap()
    else
        raw.split(",")
            .mapNotNull { entry ->
              val parts = entry.split(":")
              if (parts.size == 2) {
                val key = parts[0]
                val value = stringToValue(parts[1])
                if (value != null) key to value else null
              } else null
            }
            .toMap()
/** Encode a Map<String, Boolean> as a string. */
fun encodeBooleanMap(map: Map<String, Boolean>): String = encodeMap(map) { it.toString() }

/** Decode a Map<String, Boolean> from a string. */
fun decodeBooleanMap(raw: String): Map<String, Boolean> = decodeMap(raw) { it.toBoolean() }
