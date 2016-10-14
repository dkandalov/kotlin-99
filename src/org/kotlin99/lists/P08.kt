package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> compress(list: List<T>) =
    list.fold(emptyList<T>()) { result, value ->
        if (result.isNotEmpty() && result.last() == value) result
        else result + value
    }

class P08Test {
    @Test fun `eliminate consecutive duplicates of list elements`() {
        assertThat(compress("".toList()), equalTo("".toList()))
        assertThat(compress("abc".toList()), equalTo("abc".toList()))
        assertThat(compress("aaa".toList()), equalTo("a".toList()))
        assertThat(compress("aaaabccaadeeee".toList()), equalTo("abcade".toList()))
    }
}