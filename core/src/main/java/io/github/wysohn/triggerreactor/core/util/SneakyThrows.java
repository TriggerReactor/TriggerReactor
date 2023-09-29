package io.github.wysohn.triggerreactor.core.util;

import java.util.function.Supplier;

public final class SneakyThrows {

    public static <R> R expect(final Supplier<Boolean> condition, final Supplier<R> onSuccess, final Supplier<Throwable> onFail) {
        return expect(condition.get(), onSuccess, onFail);
    }

    public static <R> R expect(final boolean condition, final Supplier<R> onSuccess, final Supplier<Throwable> onFail) {
        if (!condition) {
            sneak(onFail);
        }

        return onSuccess.get();
    }

    public static void sneak(final Supplier<Throwable> throwable) {
        sneak(throwable.get());
    }

    public static void sneak(final Throwable throwable) {
        try {
            throw new RuntimeException(throwable);
        } catch (final Throwable e) {
            throw e;
        }
    }

    private SneakyThrows() {
        throw new UnsupportedOperationException();
    }

}