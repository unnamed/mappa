package team.unnamed.mappa.model.map.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;

public class DefaultSchemeNode implements SchemeNode {
    @NotNull
    private final Type type;
    @NotNull
    private final String name;
    private boolean optional;
    @Nullable
    private final String[] args;


    public DefaultSchemeNode(@NotNull String name, @NotNull Type type, boolean optional) {
        this(name, type, optional, null);
    }

    public DefaultSchemeNode(@NotNull String name, @NotNull Type type, boolean optional, @Nullable String[] args) {
        this.name = name;
        this.type = type;
        this.args = args;
        this.optional = optional;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public Type getType() {
        return type;
    }

    @Override
    @Nullable
    public String[] getArgs() {
        return args;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public void setOptional(boolean optional) {
        this.optional = optional;
    }


    @Override
    public String toString() {
        return "DefaultSchemeNode{" +
            "type=" + type +
            ", optional=" + optional +
            ", args=" + Arrays.toString(args) +
            '}';
    }
}
