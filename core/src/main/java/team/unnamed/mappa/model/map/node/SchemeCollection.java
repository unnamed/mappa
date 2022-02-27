package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public interface SchemeCollection extends SchemeNode {
    @NotNull Type getCollection();
}
