package team.unnamed.mappa.model.map;


public class MapProperty {
    private final String node;
    private final Object value;

    public MapProperty(String node, Object value) {
        this.node = node;
        this.value = value;
    }

    public String getNode() {
        return node;
    }

    public Object getValue() {
        return value;
    }
}
