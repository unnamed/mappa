package team.unnamed.mappa.model.map.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.Condition;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseRuntimeException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MapListProperty implements MapCollectionProperty {
    protected final List<Object> listValue;

    protected final MapNodeProperty<?> delegate;

    public MapListProperty(MapNodeProperty<?> delegate) {
        this(new ArrayList<>(), delegate);
    }

    public MapListProperty(List<Object> listValue, MapNodeProperty<?> delegate) {
        this.listValue = listValue;
        this.delegate = delegate;
    }

    @Override
    public void parseValue(@NotNull Object newValue) {
        if (newValue instanceof List) {
            List<?> list = (List<?>) newValue;
            list.forEach(this::parseValue);
            return;
        }

        Class<?> type = delegate.getType();
        Class<?> valueClass = newValue.getClass();
        if (!type.isAssignableFrom(valueClass)) {
            Object serialize = delegate.serialize(newValue);
            if (serialize == null) {
                throw new ParseRuntimeException(
                    TranslationNode.INVALID_TYPE.withFormal(
                        "{type}", type.getSimpleName()
                    )
                );
            }
            newValue = serialize;
        }

        Condition condition = getCondition();
        TextNode errMessage = condition.pass(newValue);
        if (errMessage != null) {
            throw new ParseRuntimeException(errMessage);
        }

        bypassParseValue(newValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bypassParseValue(Object newValue) {
        Function<Object, Object> postProcessing = (Function<Object, Object>) getPostProcessing();
        listValue.add(postProcessing.apply(newValue));
    }

    @Override
    public void clearValue() {
        this.listValue.clear();
    }

    @Override
    public void remove(Object value) {
        listValue.remove(value);
    }

    @Override
    public Object getValue(int slot) {
        return listValue.get(slot);
    }

    @Override
    public @Nullable Object getValue() {
        return listValue;
    }

    @Override
    public Type getType() {
        return delegate.getType();
    }

    @Override
    public @NotNull String getName() {
        return delegate.getName();
    }

    @Override
    public Condition getCondition() {
        return delegate.getCondition();
    }

    @Override
    public @NotNull Function<?, ?> getPostProcessing() {
        return delegate.getPostProcessing();
    }

    public MapNodeProperty<?> getDelegate() {
        return delegate;
    }

    @Override
    public boolean isOptional() {
        return delegate.isOptional();
    }

    @Override
    public TextNode verify(MapSession session) {
        return delegate.verify(session);
    }

    @Override
    public MapProperty clone() {
        return new MapListProperty(delegate.clone());
    }

    @Override
    public boolean isEmpty() {
        return listValue.isEmpty();
    }

    @Override
    public String toString() {
        return "MapListProperty" + listValue.toString();
    }
}
