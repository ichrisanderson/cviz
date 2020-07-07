package com.chrisa.covid19.core.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

class TestObserver<T> : Observer<T> {
    private val _values = mutableListOf<T>()
    val values: List<T>
        get() = _values

    override fun onChanged(t: T) {
        _values.add(t)
    }
}

fun <T> LiveData<T>.test() = TestObserver<T>().also { observeForever(it) }
