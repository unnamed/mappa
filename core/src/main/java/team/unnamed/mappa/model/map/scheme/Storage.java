package team.unnamed.mappa.model.map.scheme;

import java.util.function.Function;

public interface Storage {

    <T> T getObject(Key<T> key);

    <T> T getObject(Key<T> key, Function<String, T> provide);
}
