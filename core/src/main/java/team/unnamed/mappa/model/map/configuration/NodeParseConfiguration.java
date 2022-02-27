package team.unnamed.mappa.model.map.configuration;

public abstract class NodeParseConfiguration {
    protected final String node;

    protected NodeParseConfiguration(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }
}
