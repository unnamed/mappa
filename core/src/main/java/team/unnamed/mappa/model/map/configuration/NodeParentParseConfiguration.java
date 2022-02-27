package team.unnamed.mappa.model.map.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeParentParseConfiguration extends NodeParseConfiguration {
    @NotNull
    private final InterpretMode mode;
    @NotNull
    private final String formatName;
    @Nullable
    private final String[] aliases;

    public NodeParentParseConfiguration(String node,
                                        @NotNull InterpretMode mode,
                                        @NotNull String formatName,
                                        @Nullable String[] aliases) {
        super(node);
        this.mode = mode;
        this.formatName = formatName;
        this.aliases = aliases;
    }

    @Override
    public String toString() {
        return "MapParentParseConfiguration{" +
            "mode=" + mode +
            ", formatName='" + formatName + '\'' +
            ", aliases=" + Arrays.toString(aliases) +
            ", node='" + node + '\'' +
            '}';
    }
}
