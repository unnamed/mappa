package team.unnamed.mappa.model.map.configuration;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeParentParseConfiguration extends NodeParseConfiguration {
    public static final String NODE = "parent";
    public static final String PARENT_CONFIGURATION = "parent_config";

    @Nullable
    private final String formatName;
    @Nullable
    private final String pathFolder;
    @Nullable
    private final String[] aliases;

    public NodeParentParseConfiguration(@Nullable String formatName,
                                        @Nullable String pathFolder,
                                        @Nullable String[] aliases) {
        super(NODE);
        this.formatName = formatName;
        this.pathFolder = pathFolder;
        this.aliases = aliases;
    }

    public @Nullable String getFormatName() {
        return formatName;
    }

    public @Nullable String getPathFolder() {
        return pathFolder;
    }

    public String[] getAliases() {
        return aliases;
    }

    @Override
    public String toString() {
        return "MapParentParseConfiguration{" +
            ", formatName='" + formatName + '\'' +
            ", aliases=" + Arrays.toString(aliases) +
            ", node='" + pathFolder + '\'' +
            '}';
    }
}
