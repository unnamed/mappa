package team.unnamed.mappa.model.map.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Condition;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.throwable.ParseRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MapListProperty implements MapCollectionProperty {
    protected final List<Object> listValue;

    protected final MapNodeProperty delegate;

    public MapListProperty(MapNodeProperty delegate) {
        this(new ArrayList<>(), delegate);
    }

    public MapListProperty(List<Object> listValue, MapNodeProperty delegate) {
        this.listValue = listValue;
        this.delegate = delegate;
    }

    @Override
    public void parseValue(Object newValue) {
        if (newValue instanceof List) {
            List<?> list = (List<?>) newValue;
            list.forEach(this::parseValue);
            return;
        }
        Condition condition = getCondition();
        TextNode errMessage = condition.pass(newValue);
        if (errMessage != null) {
            throw new ParseRuntimeException(errMessage);
        }

        Function<Object, Object> postProcessing = getPostProcessing();
        listValue.add(postProcessing.apply(newValue));
    }

    @Override
    public void bypassParseValue(Object newValue) {
        Function<Object, Object> postProcessing = getPostProcessing();
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
    public @NotNull String getName() {
        return delegate.getName();
    }

    @Override
    public Condition getCondition() {
        return delegate.getCondition();
    }

    @Override
    public @NotNull Function<Object, Object> getPostProcessing() {
        return delegate.getPostProcessing();
    }

    @Override
    public boolean isOptional() {
        return delegate.isOptional();
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
        return listValue.toString();
    }
}
