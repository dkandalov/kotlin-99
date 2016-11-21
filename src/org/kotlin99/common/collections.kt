package org.kotlin99.common

import java.util.*

fun <T> List<T>.tail(): List<T> = drop(1)

fun <T> Array<out T>.tail(): List<T> = drop(1)

fun <T> Iterable<T>.tail(): List<T> = drop(1)

fun <T> Iterable<T>.toSeq(): Sequence<T> {
    val iterator = this.iterator()
    return object: Sequence<T> {
        override fun iterator(): Iterator<T> {
            return iterator
        }
    }
}

fun <T> ArrayList<T>.fill(n: Int, value: T): ArrayList<T> {
    1.rangeTo(n).forEach { add(value) }
    return this
}
