package team.unnamed.mappa.object.serialization;

import java.util.List;

public interface SerializableList<T> {

    T serialize(List<String> list);
}
