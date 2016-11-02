package org.kotlin99.common

fun <T> List<T>.tail(): List<T> = drop(1)
fun <T> Array<out T>.tail(): List<T> = drop(1)
fun <T> Iterable<T>.tail(): List<T> = drop(1)