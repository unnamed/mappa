package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.NodeKey;

import java.lang.reflect.Type;
import java.util.Objects;

public interface SchemeNode {

    static SchemeNode.Builder builder() {
        return new Builder();
    }

    static SchemeNode newNode(@NotNull String name,
                              @NotNull Type type) {
        return builder().name(name).type(type).optional(isNameOptional(name)).build();
    }

    static SchemeNode newNode(@NotNull String name,
                              @NotNull Type type,
                              String... args) {
        return builder().name(name).type(type).optional(isNameOptional(name)).args(args).build();
    }

    static SchemeNode newNode(@NotNull String name,
                              @NotNull Type type,
                              boolean optional) {
        return builder().name(name).type(type).optional(optional).build();
    }

    static SchemeNode newNode(@NotNull String name,
                              @NotNull Type type,
                              boolean optional,
                              String... args) {
        return builder().name(name).type(type).optional(optional).args(args).build();
    }

    static SchemeNode newNode(@NotNull String name,
                              @NotNull Type type,
                              @Nullable String tag,
                              boolean optional,
                              String... args) {
        return new DefaultSchemeNode(name, type, tag, optional, args);
    }

    static SchemeCollection newCollection(@NotNull String name, @NotNull Type collection, @NotNull SchemeNode typeNode) {
        return new DefaultSchemeCollection(name, collection, typeNode);
    }

    static boolean isNameOptional(String name) {
        return name.charAt(name.length() - 1) == '?';
    }

    @NotNull String getName();

    @Nullable String getTag();

    @NotNull Type getType();

    String[] getArgs();

    boolean isOptional();

    void setOptional(boolean optional);

    default NodeKey toKey() {
        return new NodeKey(getTag(), getType());
    }

    class Builder {
        private String name;
        private Type type;
        private @Nullable String tag;
        private boolean optional;
        private @Nullable String[] args;

        public Builder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        public Builder type(@NotNull Type type) {
            this.type = type;
            return this;
        }

        public Builder tag(@Nullable String tag) {
            this.tag = tag;
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder args(@Nullable String[] args) {
            this.args = args;
            return this;
        }

        public SchemeNode build() {
            return new DefaultSchemeNode(Objects.requireNonNull(name),
                Objects.requireNonNull(type),
                tag,
                optional,
                args);
        }
    }
}
