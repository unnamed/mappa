package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class DefaultSchemeCollection implements SchemeCollection {
    @NotNull
    private final Type collection;
    @NotNull
    private final SchemeNode typeNode;

    public DefaultSchemeCollection(@NotNull Type collection, @NotNull SchemeNode typeNode) {
        this.collection = collection;
        this.typeNode = typeNode;
    }

    @Override
    public @NotNull Type getCollection() {
        return collection;
    }

    @Override
    public @NotNull Type getType() {
        return typeNode.getType();
    }

    @Override
    public @Nullable String[] getArgs() {
        return typeNode.getArgs();
    }

    @Override
    public boolean isOptional() {
        return typeNode.isOptional();
    }

    @Override
    public void setOptional(boolean optional) {
        typeNode.setOptional(optional);
    }

    @Override
    public String toString() {
        return "DefaultSchemaCollection{" +
            "collection=" + collection +
            ", typeNode=" + typeNode +
            '}';
    }
}
