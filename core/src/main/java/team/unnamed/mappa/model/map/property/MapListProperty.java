package team.unnamed.mappa.model.map.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MapListProperty implements MapCollectionProperty {
    protected final List<Object> listValue;

    protected final MapProperty delegate;

    public MapListProperty(MapProperty delegate) {
        this(new ArrayList<>(), delegate);
    }

    public MapListProperty(List<Object> listValue, MapProperty delegate) {
        this.listValue = listValue;
        this.delegate = delegate;
    }

    @Override
    public void parseValue(Object newValue) {
        Condition condition = getCondition();
        String errMessage = condition.pass(newValue);
        if (errMessage != null) {
            return;
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
    public boolean isBuildProperty() {
        return delegate.isBuildProperty();
    }

    @Override
    public boolean isEmpty() {
        return listValue.isEmpty();
    }
}
