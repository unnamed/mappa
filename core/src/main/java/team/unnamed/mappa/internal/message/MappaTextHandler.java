package team.unnamed.mappa.internal.message;

import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import team.unnamed.mappa.object.TextNode;

public class MappaTextHandler {
    protected final MessageHandler delegate;

    public MappaTextHandler(MessageHandler delegate) {
        this.delegate = delegate;
    }

    public String format(Object entity, TextNode node) {
        return delegate.replacing(entity, node.getNode(), node.getPlaceholders());
    }

    public String format(Object entity, TextNode node, Object... entities) {
        return delegate.format(entity, node.getNode(), ReplacePack.make(node.getPlaceholders()), entities);
    }

    public MessageHandler getDelegate() {
        return delegate;
    }
}
