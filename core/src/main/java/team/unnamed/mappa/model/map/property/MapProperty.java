package team.unnamed.mappa.model.map.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Condition;

import java.util.function.Function;

public interface MapProperty extends Cloneable {

    void parseValue(Object newValue);

    void bypassParseValue(Object newValue);

    void clearValue();

    @Nullable Object getValue();

    Condition getCondition();

    @NotNull String getName();

    @NotNull Function<Object, Object> getPostProcessing();

    boolean isOptional();

    MapProperty clone();
}
