package team.unnamed.mappa.object;

public interface Text {

    static TextNode withFormal(String node, Object... placeholders) {
        return new TextNode(node, placeholders, true);
    }

    static TextNode with(String node, Object... placeholders) {
        return new TextNode(node, placeholders, false);
    }

    default TextNode with(Object... objects) {
        return with(getNode(), objects);
    }

    default TextNode withFormal(Object... objects) {
        return Text.withFormal(getNode(), objects);
    }

    default TextNode text() {
        return Text.with(getNode());
    }

    default TextNode formalText() {
        return Text.withFormal(getNode());
    }

    String getNode();

    Object[] getPlaceholders();

    boolean isFormal();
}
