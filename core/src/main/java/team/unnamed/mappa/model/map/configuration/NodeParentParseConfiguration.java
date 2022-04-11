package team.unnamed.mappa.model.map.configuration;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeParentParseConfiguration extends NodeParseConfiguration {
    public static final String PARENT_CONFIGURATION = "parent_config";

    @Nullable
    private final String formatName;
    @Nullable
    private final String[] aliases;

    public NodeParentParseConfiguration(@Nullable String formatName,
                                        @Nullable String[] aliases) {
        super("parent");
        this.formatName = formatName;
        this.aliases = aliases;
    }

    public @Nullable String getFormatName() {
        return formatName;
    }

    public String[] getAliases() {
        return aliases;
    }

    @Override
    public String toString() {
        return "MapParentParseConfiguration{" +
            ", formatName='" + formatName + '\'' +
            ", aliases=" + Arrays.toString(aliases) +
            ", node='" + path + '\'' +
            '}';
    }
}
