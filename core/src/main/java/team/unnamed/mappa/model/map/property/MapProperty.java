package team.unnamed.mappa.model.map.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Condition;

import java.lang.reflect.Type;
import java.util.function.Function;

public interface MapProperty extends Cloneable {

    void parseValue(@NotNull Object newValue);

    void bypassParseValue(Object newValue);

    void clearValue();

    @Nullable Object getValue();

    Type getType();

    Condition getCondition();

    @NotNull String getName();

    @NotNull Function<?, ?> getPostProcessing();

    boolean isOptional();

    MapProperty clone();
}
