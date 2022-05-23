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

    static SchemeCollection newCollection(@NotNull String name, @NotNull Type collection, @NotNull SchemeNode typeNode) {
        return new DefaultSchemeCollection(name, collection, typeNode);
    }

    @NotNull String getName();

    @Nullable String[] getAliases();

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
        private @Nullable String[] aliases;

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
            return optional(optional, false);
        }

        public Builder optional(boolean optional, boolean removeSuffix) {
            this.optional = optional;
            if (removeSuffix) {
                char lastChar = this.name.charAt(name.length() - 1);
                if (lastChar == '?') {
                    this.name = name.substring(0, name.length() - 1);
                }
            }
            return this;
        }

        public Builder args(@Nullable String[] args) {
            this.args = args;
            return this;
        }

        public Builder aliases(@Nullable String[] aliases) {
            this.aliases = aliases;
            return this;
        }

        public SchemeNode build() {
            return new DefaultSchemeNode(Objects.requireNonNull(name),
                Objects.requireNonNull(type),
                tag,
                optional,
                args,
                aliases);
        }
    }
}
