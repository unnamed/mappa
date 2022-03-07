package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class DefaultSchemeCollection implements SchemeCollection {
    @NotNull
    private final String name;
    @NotNull
    private final Type collection;
    @NotNull
    private final SchemeNode typeNode;

    public DefaultSchemeCollection(@NotNull String name, @NotNull Type collection, @NotNull SchemeNode typeNode) {
        this.name = name;
        this.collection = collection;
        this.typeNode = typeNode;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull SchemeNode getTypeNode() {
        return typeNode;
    }

    @Override
    public @NotNull Type getCollectionType() {
        return collection;
    }


    @Override
    public @Nullable String getTag() {
        return typeNode.getTag();
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
