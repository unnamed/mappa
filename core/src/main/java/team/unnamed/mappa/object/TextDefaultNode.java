package team.unnamed.mappa.object;

public class TextDefaultNode extends TextNode implements TextDefault {
    private final String defaultMessage;

    public TextDefaultNode(String node, String defaultMessage) {
        super(node, null, false);
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public TextNode with(Object... placeholders) {
        return Text.with(getNode(), placeholders);
    }

    @Override
    public TextNode withFormal(Object... placeholders) {
        return Text.with(getNode(), placeholders);
    }
}
