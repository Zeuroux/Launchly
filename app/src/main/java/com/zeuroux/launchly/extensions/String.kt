package com.zeuroux.launchly.extensions

fun String.toBoolean(): Boolean = when(this.lowercase()) {
    "0" -> false
    "1" -> true
    "true" -> true
    "false" -> false
    else -> throw IllegalArgumentException("$this cannot be converted to boolean, expected 0 or 1")
}