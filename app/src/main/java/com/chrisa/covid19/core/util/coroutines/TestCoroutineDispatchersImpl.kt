package com.chrisa.covid19.core.util.coroutines

import kotlinx.coroutines.CoroutineDispatcher

class TestCoroutineDispatchersImpl(private val dispatcher: CoroutineDispatcher) :
    CoroutineDispatchers {
    override val io: CoroutineDispatcher
        get() = dispatcher
    override val main: CoroutineDispatcher
        get() = dispatcher

}
