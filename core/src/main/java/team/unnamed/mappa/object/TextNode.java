package team.unnamed.mappa.object;

import java.util.Arrays;

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
        return "TextNode{" +
            "node='" + node + '\'' +
            ", placeholders=" + Arrays.toString(placeholders) +
            '}';
    }
}
