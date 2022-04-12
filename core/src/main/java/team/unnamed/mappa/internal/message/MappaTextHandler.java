package team.unnamed.mappa.internal.message;

import me.yushust.message.MessageHandler;
import me.yushust.message.config.ConfigurationModule;
import me.yushust.message.source.MessageSource;
import me.yushust.message.source.MessageSourceDecorator;
import me.yushust.message.util.ReplacePack;
import team.unnamed.mappa.object.TextNode;

public class MappaTextHandler {
    protected final MessageHandler delegate;

    protected final String prefixNode;

    public static MappaTextHandler fromSource(String fallbackLang,
                                              String prefixNode,
                                              MessageSource source,
                                              ConfigurationModule... handles) {
        MessageSourceDecorator decorator = MessageSourceDecorator.decorate(source);
        decorator.addFallbackLanguage(fallbackLang);
        return fromSource(prefixNode, decorator.get(), handles);
    }

    public static MappaTextHandler fromSource(String prefixNode,
                                              MessageSource source,
                                              ConfigurationModule... handles) {
        return new MappaTextHandler(
            MessageHandler.of(source, handles), prefixNode);
    }

    public MappaTextHandler(MessageHandler delegate, String prefixNode) {
        this.delegate = delegate;
        this.prefixNode = prefixNode;
    }

    public String format(Object entity, String node) {
        return delegate.replacing(entity, node);
    }

    public String format(Object entity, TextNode node) {
        return delegate.replacing(entity, node.getNode(), node.getPlaceholders());
    }

    public String format(Object entity, TextNode node, Object... entities) {
        return delegate.format(entity, node.getNode(), ReplacePack.make(node.getPlaceholders()), entities);
    }

    public void send(Object entity, TextNode node, Object... entities) {
        String prefix = node.isFormal() && prefixNode != null
            ? format(entity, prefixNode)
            : "";
        delegate.dispatch(entity,
            node.getNode(),
            prefix,
            ReplacePack.make(node.getPlaceholders()),
            entities);
    }

    public MessageHandler getDelegate() {
        return delegate;
    }
}
