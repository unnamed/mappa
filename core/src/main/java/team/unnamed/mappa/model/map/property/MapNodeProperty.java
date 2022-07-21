package team.unnamed.mappa.model.map.property;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.object.Condition;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.object.serialization.Serializable;
import team.unnamed.mappa.object.serialization.SerializableList;
import team.unnamed.mappa.throwable.ParseRuntimeException;
import team.unnamed.mappa.util.TypeUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class MapNodeProperty<T> implements MapProperty {
    @NotNull
    protected final String name;
    @Nullable
    protected final String[] aliases;
    @NotNull
    protected final Class<T> type;
    @NotNull
    protected final Condition condition;

    @NotNull
    protected final Function<T, T> postProcessing;
    protected final Serializable<T> serializable;
    protected final SerializableList<T> serializableList;

    @Nullable
    protected final Function<MapEditSession, TextNode> postVerification;
    protected final boolean optional;
    protected final boolean ignore;
    protected final boolean firstAlias;
    protected final boolean readOnly;
    protected Object value;

    public static <T> Builder<T> builder(String node, Class<T> clazz) {
        return new Builder<>(node, clazz);
    }

    public static <T> Builder<T> builder(MapNodeProperty<T> property) {
        return builder(property.name, property.type)
            .condition(property.condition)
            .postProcessing(property.postProcessing)
            .serializable(property.serializable)
            .serializableList(property.serializableList)
            .postVerification(property.postVerification)
            .aliases(property.aliases)
            // .readOnly(property.readOnly)
            .ignore(property.ignore)
            .firstAlias(property.firstAlias)
            .optional(property.optional);
    }

    public MapNodeProperty(@NotNull String name,
                           @Nullable String[] aliases,
                           @NotNull Class<T> type,
                           @NotNull Condition condition,
                           @NotNull Function<T, T> postProcessing,
                           Serializable<T> serializable,
                           SerializableList<T> serializableList,
                           @Nullable Function<MapEditSession, TextNode> postVerification,
                           boolean optional,
                           boolean ignore,
                           boolean firstAlias,
                           boolean readOnly) {
        this.name = name;
        this.aliases = aliases;
        this.type = (Class<T>) TypeUtils.primitiveToWrapper(type);
        this.condition = condition;
        this.postProcessing = postProcessing;
        this.serializable = serializable;
        this.serializableList = serializableList;
        this.postVerification = postVerification;
        this.optional = optional;
        this.ignore = ignore;
        this.firstAlias = firstAlias;
        this.readOnly = readOnly;
    }

    @Override
    public void parseValue(@NotNull Object newValue) {
        if (readOnly) {
            throw new ParseRuntimeException(
                TranslationNode.PROPERTY_READ_ONLY);
        }

        Class<?> valueClass = TypeUtils.primitiveToWrapper(newValue.getClass());
        if (!type.isAssignableFrom(valueClass)) {
            Object serialize = serialize(newValue);
            if (serialize == null) {
                throw new ParseRuntimeException(
                    TranslationNode.INVALID_TYPE.withFormal(
                        "{name}", getName(),
                        "{parameter}", newValue,
                        "{type}", type.getSimpleName()
                    )
                );
            }
            newValue = serialize;
        }


        TextNode errorMessage = condition.pass(newValue);
        if (errorMessage != null) {
            throw new ParseRuntimeException(errorMessage);
        }

        this.value = postProcessing.apply((T) newValue);
    }

    public Object serialize(Object object) {
        if (serializable != null && object instanceof String) {
            return serializable.serialize((String) object);
        } else if (serializableList != null && object instanceof Collection) {
            Collection<?> list = (Collection<?>) object;
            return serializableList.serialize(
                list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList())
            );
        } else {
            return null;
        }
    }

    @Override
    public void bypassParseValue(Object newValue) {
        this.value = newValue;
    }

    @Override
    public void clearValue() {
        this.value = null;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return aliases;
    }

    @Override
    @NotNull
    public Function<T, T> getPostProcessing() {
        return postProcessing;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean isIgnore() {
        return ignore;
    }

    @Override
    public boolean isFirstAlias() {
        return firstAlias;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean hasVerification() {
        return postVerification != null;
    }

    @Override
    public Text verify(MapEditSession session) {
        return postVerification == null
            ? null
            : postVerification.apply(session);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public MapNodeProperty<T> clone() {
        return toBuilder().build();
    }

    @Override
    @Nullable
    public Object getValue() {
        return value;
    }

    @Override
    public @NotNull Class<T> getType() {
        return type;
    }

    @Override
    public @NotNull Condition getCondition() {
        return condition;
    }

    public Builder<T> toBuilder() {
        return builder(this);
    }

    @Override
    public String toString() {
        return "MapNodeProperty{" +
            "name='" + name + '\'' +
            ", optional=" + optional +
            ", value=" + value +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapNodeProperty<?> that = (MapNodeProperty<?>) o;
        return optional == that.optional
            && name.equals(that.name)
            && type.equals(that.type)
            && condition.equals(that.condition)
            && postProcessing.equals(that.postProcessing)
            && Objects.equals(serializable, that.serializable)
            && Objects.equals(serializableList, that.serializableList)
            && Objects.equals(postVerification, that.postVerification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,
            type,
            condition,
            postProcessing,
            serializable,
            serializableList,
            postVerification,
            optional);
    }

    public static class Builder<T> {
        private final String node;
        private String[] aliases;
        private final Class<T> type;
        private Condition condition;
        private Serializable<T> serializable;
        private SerializableList<T> serializableList;
        private Function<T, T> postProcessing;
        private Function<MapEditSession, TextNode> verification;
        private boolean optional;
        private boolean ignore;
        private boolean firstAlias;
        private boolean readOnly;

        public Builder(String node, Class<T> type) {
            this.node = node;
            this.type = type;
        }

        public Builder<T> aliases(String... aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder<T> condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Builder<T> serializable(Serializable<T> serializable) {
            this.serializable = serializable;
            return this;
        }

        public Builder<T> serializableList(SerializableList<T> serializableList) {
            this.serializableList = serializableList;
            return this;
        }

        public Builder<T> postProcessing(Function<T, T> postProcessing) {
            this.postProcessing = postProcessing;
            return this;
        }

        public Builder<T> postVerification(Function<MapEditSession, TextNode> verification) {
            this.verification = verification;
            return this;
        }

        public Builder<T> optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder<T> ignore(boolean ignore) {
            this.ignore = ignore;
            return this;
        }

        public Builder<T> firstAlias(boolean firstAlias) {
            this.firstAlias = firstAlias;
            return this;
        }

        public Builder<T> readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public MapNodeProperty<T> build() {
            if (postProcessing == null) {
                postProcessing = Function.identity();
            }
            if (condition == null) {
                condition = Condition.EMPTY;
            }
            return new MapNodeProperty<>(node,
                aliases,
                type,
                condition,
                postProcessing,
                serializable,
                serializableList,
                verification,
                optional,
                ignore,
                firstAlias,
                readOnly);
        }
    }
}
