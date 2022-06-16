package team.unnamed.mappa.object;

public class TextNode implements Text {
    private final String node;
    private final Object[] placeholders;

    private final boolean formal;

    public TextNode(String node, Object[] placeholders, boolean formal) {
        this.node = node;
        this.placeholders = placeholders;
        this.formal = formal;
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public Object[] getPlaceholders() {
        return placeholders;
    }

    @Override
    public boolean isFormal() {
        return formal;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
