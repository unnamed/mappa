package team.unnamed.mappa.object.config;

import java.util.List;

public interface LineSerializableList<T> {

    T serialize(List<String> list);
}
