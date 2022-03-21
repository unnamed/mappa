package team.unnamed.mappa.model.map.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeParentParseConfiguration extends NodeParseConfiguration {
    public static final String PARENT_CONFIGURATION = "parent_config";

    @NotNull
    private final InterpretMode mode;
    @NotNull
    private final String formatName;
    @Nullable
    private final String[] aliases;

    public NodeParentParseConfiguration(@NotNull InterpretMode mode,
                                        @NotNull String formatName,
                                        @Nullable String[] aliases) {
        super("parent");
        this.mode = mode;
        this.formatName = formatName;
        this.aliases = aliases;
    }

    public @NotNull InterpretMode getMode() {
        return mode;
    }

    public @NotNull String getFormatName() {
        return formatName;
    }

    public String[] getAliases() {
        return aliases;
    }

    @Override
    public String toString() {
        return "MapParentParseConfiguration{" +
            "mode=" + mode +
            ", formatName='" + formatName + '\'' +
            ", aliases=" + Arrays.toString(aliases) +
            ", node='" + path + '\'' +
            '}';
    }
}
