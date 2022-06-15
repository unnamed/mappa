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
    @Nullable
    private final String tag;
    private boolean optional;
    private final boolean firstAlias;
    @Nullable
    private final String[] args;
    @Nullable
    private final String[] aliases;

    public DefaultSchemeNode(@NotNull String name,
                             @NotNull Type type,
                             @Nullable String tag,
                             boolean optional,
                             boolean firstAlias,
                             @Nullable String[] args,
                             @Nullable String[] aliases) {
        this.name = name;
        this.type = type;
        this.tag = tag;
        this.args = args;
        this.optional = optional;
        this.firstAlias = firstAlias;
        System.out.println("firstAlias = " + firstAlias);
        this.aliases = aliases;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getTag() {
        return tag;
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
    public boolean isFirstAlias() {
        return firstAlias;
    }

    @Override
    public void setOptional(boolean optional) {
        this.optional = optional;
    }


    @Override
    public String toString() {
        return "DefaultSchemeNode{" +
            "type=" + type +
            ", name='" + name + '\'' +
            ", tag='" + tag + '\'' +
            ", optional=" + optional +
            ", args=" + Arrays.toString(args) +
            ", aliases=" + Arrays.toString(aliases) +
            '}';
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return aliases;
    }
}
