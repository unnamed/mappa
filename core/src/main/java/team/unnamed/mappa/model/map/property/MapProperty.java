package team.unnamed.mappa.model.map.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.Condition;
import team.unnamed.mappa.object.Text;

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

    @Nullable String[] getAliases();

    @NotNull Function<?, ?> getPostProcessing();

    boolean isOptional();

    boolean isReadOnly();

    boolean hasVerification();

    /**
     * Verify the integrity of this property with the session.
     * @param session Session to check.
     * @return error message.
     */
    Text verify(MapSession session);

    MapProperty clone();
}
