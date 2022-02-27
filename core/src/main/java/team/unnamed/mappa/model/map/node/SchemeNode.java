package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface SchemeNode {

    static SchemeNode newNode(Type type,
                              boolean optional) {
        return new DefaultSchemeNode(type, optional);
    }

    static SchemeNode newNode(Type type,
                              boolean optional,
                              String... args) {
        return new DefaultSchemeNode(type, optional, args);
    }

    static SchemeCollection newCollection(@NotNull Type collection, @NotNull SchemeNode typeNode) {
        return new DefaultSchemeCollection(collection, typeNode);
    }

    @NotNull Type getType();

    @Nullable String[] getArgs();

    boolean isOptional();

    void setOptional(boolean optional);
}
