package team.unnamed.mappa.function;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.throwable.ParseException;

public interface ParseConsumer<T> {

    void accept(@NotNull T t) throws ParseException;
}
