package team.unnamed.mappa.model.map.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiNodeParseConfiguration extends NodeParseConfiguration {
    @NotNull
    private final List<String> multiNodes;

    public MultiNodeParseConfiguration(@NotNull List<String> multiNodes) {
        super("multi-node");
        this.multiNodes = multiNodes;
    }

    public @NotNull List<String> getMultiNodes() {
        return multiNodes;
    }

    @Override
    public String toString() {
        return "MultiNodeParseConfiguration{" +
            "multiNodes=" + multiNodes +
            ", node='" + node + '\'' +
            '}';
    }
}
