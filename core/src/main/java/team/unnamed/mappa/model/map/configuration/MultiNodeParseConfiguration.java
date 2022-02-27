package team.unnamed.mappa.model.map.configuration;

import org.jetbrains.annotations.NotNull;

public class MultiNodeParseConfiguration extends NodeParseConfiguration {
    @NotNull
    private final String[] multiNodes;

    protected MultiNodeParseConfiguration(String node, @NotNull String[] multiNodes) {
        super(node);
        this.multiNodes = multiNodes;
    }

    public String[] getMultiNodes() {
        return multiNodes;
    }
}
