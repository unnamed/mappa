package team.unnamed.mappa.internal.message;

import me.yushust.message.MessageHandler;
import me.yushust.message.config.ConfigurationModule;
import me.yushust.message.source.MessageSource;
import me.yushust.message.source.MessageSourceDecorator;
import me.yushust.message.track.TrackingContext;
import me.yushust.message.util.ReplacePack;
import team.unnamed.mappa.object.Text;

import java.util.Collections;

public class MappaTextHandler {
    protected final MessageHandler delegate;

    protected final String prefixNode;

    public static MappaTextHandler fromSource(String fallbackLang,
                                              String prefixNode,
                                              MessageSource source,
                                              ConfigurationModule handle) {
        MessageSourceDecorator decorator = MessageSourceDecorator.decorate(source);
        decorator.addFallbackLanguage(fallbackLang);
        return fromSource(prefixNode,
            decorator.get(),
            handle);
    }

    public static MappaTextHandler fromSource(String prefixNode,
                                              MessageSource source,
                                              ConfigurationModule handle) {
        return new MappaTextHandler(
            MessageHandler.of(source, handle), prefixNode);
    }

    public MappaTextHandler(MessageHandler delegate,
                            String prefixNode) {
        this.delegate = delegate;
        this.prefixNode = prefixNode;
    }

    public String format(Object entity, String node) {
        return delegate.replacing(entity, node);
    }

    public String format(Object entity, Text node) {
        return delegate.replacing(entity, node.getNode(), node.getPlaceholders());
    }

    public String format(Object entity, Text node, Object... entities) {
        return delegate.format(entity, node.getNode(), ReplacePack.make(node.getPlaceholders()), entities);
    }

    public String formatLang(String language, Text node, Object... entities) {
        TrackingContext context = new TrackingContext(null,
            language,
            entities,
            ReplacePack.make(node.getPlaceholders()),
            Collections.emptyMap(),
            delegate);
        return delegate.format(context, node.getNode());
    }

    public String formatLang(String language, String node, Object... entities) {
        TrackingContext context = new TrackingContext(null,
            language,
            entities,
            ReplacePack.make(),
            Collections.emptyMap(),
            delegate);
        return delegate.format(context, node);
    }

    public void send(Object entity, Text node, Object... entities) {
        String prefix = node.isFormal() && prefixNode != null
            ? format(entity, prefixNode)
            : "";
        Object[] placeholders = node.getPlaceholders();
        delegate.dispatch(entity,
            node.getNode(),
            prefix,
            placeholders == null ? ReplacePack.EMPTY : ReplacePack.make(placeholders),
            entities);
    }

    public void send(Object entity, String node, boolean formal, Object... entities) {
        String prefix = formal
            ? format(entity, prefixNode)
            : "";
        delegate.dispatch(entity,
            node,
            prefix,
            ReplacePack.EMPTY,
            entities);
    }

    public String getPrefix(Object entity) {
        return format(entity, prefixNode);
    }

    public String getPrefix(String lang) {
        return formatLang(lang, prefixNode);
    }

    public MessageHandler getDelegate() {
        return delegate;
    }
}
