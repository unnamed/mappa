package team.unnamed.mappa.model.map;

import java.lang.reflect.Type;
import java.util.Objects;

public class NodeKey {
    private final String tag;
    private final Type type;

    public NodeKey(String tag, Type type) {
        this.tag = tag;
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeKey nodeKey = (NodeKey) o;
        return Objects.equals(tag, nodeKey.tag) && Objects.equals(type, nodeKey.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, type);
    }

    @Override
    public String toString() {
        return "NodeKey{" +
            "tag='" + tag + '\'' +
            ", type=" + type +
            '}';
    }
}
