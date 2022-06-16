package team.unnamed.mappa.object;

public class TextDefaultNode extends TextNode implements TextDefault {
    private final String defaultMessage;

    public TextDefaultNode(String node, String defaultMessage) {
        this(node, defaultMessage, false, (Object[]) null);
    }

    public TextDefaultNode(String node, String defaultMessage, boolean formal, Object... objects) {
        super(node, objects, formal);
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public TextNode with(Object... placeholders) {
        return new TextNode(getNode(), placeholders, true);
    }

    @Override
    public TextNode withFormal(Object... placeholders) {
        return new TextNode(getNode(), placeholders, true);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
