package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.NodeKey;

import java.lang.reflect.Type;

public interface SchemeCollection extends SchemeNode {

    @NotNull SchemeNode getTypeNode();

    @NotNull Type getCollectionType();

    @Override
    default NodeKey toKey() {
        return new NodeKey(null, getCollectionType());
    }
}
