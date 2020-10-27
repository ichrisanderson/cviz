package com.chrisa.cron19.core.util.coroutines;

import javax.annotation.Generated;

import dagger.internal.Factory;

@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class CoroutineDispatchersImpl_Factory implements Factory<CoroutineDispatchersImpl> {
    @Override
    public CoroutineDispatchersImpl get() {
        return newInstance();
    }

    public static CoroutineDispatchersImpl_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static CoroutineDispatchersImpl newInstance() {
        return new CoroutineDispatchersImpl();
    }

    private static final class InstanceHolder {
        private static final CoroutineDispatchersImpl_Factory INSTANCE = new CoroutineDispatchersImpl_Factory();
    }
}
