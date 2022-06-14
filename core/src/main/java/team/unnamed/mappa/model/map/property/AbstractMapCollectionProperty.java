package team.unnamed.mappa.model.map.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.Condition;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseRuntimeException;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractMapCollectionProperty implements MapCollectionProperty {
    protected final Collection<Object> values;
    protected final MapNodeProperty<?> delegate;

    public AbstractMapCollectionProperty(Collection<Object> values, MapNodeProperty<?> delegate) {
        this.values = values;
        this.delegate = delegate;
    }

    @Override
    public void parseValue(@NotNull Object newValue) {
        if (isReadOnly()) {
            throw new ParseRuntimeException(
                TranslationNode.PROPERTY_READ_ONLY);
        }

        if (newValue instanceof Collection) {
            Collection<?> list = (Collection<?>) newValue;
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
        values.add(postProcessing.apply(newValue));
    }

    @Override
    public void clearValue() {
        this.values.clear();
    }

    @Override
    public boolean remove(Object value) {
        return values.remove(value);
    }

    @Override
    public abstract Object getValue(int slot);

    @Override
    public @Nullable Object getValue() {
        return values;
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
    public @Nullable String[] getAliases() {
        return delegate.getAliases();
    }

    @Override
    public Condition getCondition() {
        return delegate.getCondition();
    }

    @Override
    public @NotNull Function<?, ?> getPostProcessing() {
        return delegate.getPostProcessing();
    }

    @Override
    public MapNodeProperty<?> getDelegate() {
        return delegate;
    }

    @Override
    public Type getCollectionType() {
        return List.class;
    }

    @Override
    public boolean isOptional() {
        return delegate.isOptional();
    }

    @Override
    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    @Override
    public boolean hasVerification() {
        return delegate.hasVerification();
    }

    @Override
    public Text verify(MapSession session) {
        return delegate.verify(session);
    }

    @Override
    public abstract MapProperty clone();

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }
}
