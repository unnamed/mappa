package team.unnamed.mappa.model.map.property;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.Condition;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.object.serialization.Serializable;
import team.unnamed.mappa.object.serialization.SerializableList;
import team.unnamed.mappa.throwable.ParseRuntimeException;
import team.unnamed.mappa.util.TypeUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class MapNodeProperty<T> implements MapProperty {
    @NotNull
    protected final String name;
    @NotNull
    protected final Class<T> type;
    @NotNull
    protected final Condition condition;

    @NotNull
    protected final Function<T, T> postProcessing;
    protected final Serializable<T> serializable;
    protected final SerializableList<T> serializableList;
    
    @Nullable
    protected final Function<MapSession, TextNode> postVerification;
    protected final boolean optional;
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
            .optional(property.optional);
    }

    public MapNodeProperty(@NotNull String name,
                           @NotNull Class<T> type,
                           @NotNull Condition condition,
                           @NotNull Function<T, T> postProcessing,
                           Serializable<T> serializable,
                           SerializableList<T> serializableList,
                           @Nullable Function<MapSession, TextNode> postVerification,
                           boolean optional) {
        this.name = name;
        this.type = (Class<T>) TypeUtils.primitiveToWrapper(type);
        this.condition = condition;
        this.postProcessing = postProcessing;
        this.serializable = serializable;
        this.serializableList = serializableList;
        this.postVerification = postVerification;
        this.optional = optional;
    }

    @Override
    public void parseValue(@NotNull Object newValue) {
        Class<?> valueClass = TypeUtils.primitiveToWrapper(newValue.getClass());
        if (!type.isAssignableFrom(valueClass)) {
            Object serialize = serialize(newValue);
            if (serialize == null) {
                throw new ParseRuntimeException(
                    TranslationNode.INVALID_TYPE.withFormal(
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
        } else if (serializableList != null && object instanceof List) {
            List<?> list = (List<?>) object;
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
    public Text verify(MapSession session) {
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

    public static class Builder<T> {
        private final String node;
        private final Class<T> type;
        private Condition condition;
        private Serializable<T> serializable;
        private SerializableList<T> serializableList;
        private Function<T, T> postProcessing;
        private Function<MapSession, TextNode> verification;
        private boolean optional;

        public Builder(String node, Class<T> type) {
            this.node = node;
            this.type = type;
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

        public Builder<T> postVerification(Function<MapSession, TextNode> verification) {
            this.verification = verification;
            return this;
        }  
        
        public Builder<T> optional(boolean optional) {
            this.optional = optional;
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
                type,
                condition,
                postProcessing,
                serializable,
                serializableList,
                verification, 
                optional);
        }
    }
}
