package team.unnamed.mappa.util;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.function.ParseConsumer;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Objects;

public interface ParseUtils {

    static <T> void forEach(@Nullable T[] array,
                            ParseConsumer<T> consumer) throws ParseException {
        if (array == null) {
            return;
        }

        for (T t : array) {
            consumer.accept(Objects.requireNonNull(t, "Arg is null"));
        }
    }
}
