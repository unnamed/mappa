package team.unnamed.mappa.model.map.configuration;

public abstract class NodeParseConfiguration {
    protected final String path;

    protected NodeParseConfiguration(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
