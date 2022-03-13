package team.unnamed.mappa.object;

public class TextNode {
    private final String node;
    private final Object[] placeholders;

    private final boolean formal;

    public static TextNode withFormal(String node, Object... placeholders) {
        return new TextNode(node, placeholders, true);
    }

    public static TextNode with(String node, Object... placeholders) {
        return new TextNode(node, placeholders, false);
    }

    public TextNode(String node, Object[] placeholders, boolean formal) {
        this.node = node;
        this.placeholders = placeholders;
        this.formal = formal;
    }

    public String getNode() {
        return node;
    }

    public Object[] getPlaceholders() {
        return placeholders;
    }

    public boolean isFormal() {
        return formal;
    }
}
