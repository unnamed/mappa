package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface SchemeNode {

    static SchemeNode newNode(@NotNull String name,
                              Type type) {
        return new DefaultSchemeNode(name, type, isNameOptional(name));
    }

    static SchemeNode newNode(@NotNull String name,
                              Type type,
                              String... args) {
        return new DefaultSchemeNode(name, type, isNameOptional(name), args);
    }

    static SchemeNode newNode(@NotNull String name,
                              Type type,
                              boolean optional) {
        return new DefaultSchemeNode(name, type, optional);
    }

    static SchemeNode newNode(@NotNull String name,
                              Type type,
                              boolean optional,
                              String... args) {
        return new DefaultSchemeNode(name, type, optional, args);
    }

    static SchemeCollection newCollection(@NotNull String name, @NotNull Type collection, @NotNull SchemeNode typeNode) {
        return new DefaultSchemeCollection(name, collection, typeNode);
    }

    static boolean isNameOptional(String name) {
        return name.charAt(name.length() - 1) == '?';
    }

    @NotNull String getName();

    @NotNull Type getType();

    @Nullable String[] getArgs();

    boolean isOptional();

    void setOptional(boolean optional);
}
