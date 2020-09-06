package com.demo.demoautovalidation.common;

import java.util.Optional;
import java.util.function.Consumer;

public class OptionalConsumer<T> {

    private Optional<T> optional;

    private OptionalConsumer(Optional<T> optional) {
        this.optional = optional;
    }

    public static <T> OptionalConsumer<T> of(Optional<T> optional) {
        return new OptionalConsumer<>(optional);
    }

    public OptionalConsumer<T> ifPresent(Consumer<T> consumer) {
        optional.ifPresent(consumer);
        return this;
    }

    public void ifNotPresent(Runnable runnable) {
        if (!optional.isPresent()) {
            runnable.run();
        }
    }
}
