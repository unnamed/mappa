package team.unnamed.mappa.internal.message;

import me.yushust.message.MessageHandler;
import me.yushust.message.config.ConfigurationModule;
import me.yushust.message.source.MessageSource;
import me.yushust.message.source.MessageSourceDecorator;
import me.yushust.message.util.ReplacePack;
import team.unnamed.mappa.object.TextNode;

public class MappaTextHandler {
    protected final MessageHandler delegate;

    public static MappaTextHandler fromSource(String fallbackLang,
                                              MessageSource source,
                                              ConfigurationModule... handles) {
        MessageSourceDecorator decorator = MessageSourceDecorator.decorate(source);
        decorator.addFallbackLanguage(fallbackLang);
        return fromSource(decorator.get(), handles);
    }

    public static MappaTextHandler fromSource(MessageSource source, ConfigurationModule... handles) {
        return new MappaTextHandler(
            MessageHandler.of(source, handles));
    }

    public MappaTextHandler(MessageHandler delegate) {
        this.delegate = delegate;
    }

    public String format(Object entity, TextNode node) {
        return delegate.replacing(entity, node.getNode(), node.getPlaceholders());
    }

    public String format(Object entity, TextNode node, Object... entities) {
        return delegate.format(entity, node.getNode(), ReplacePack.make(node.getPlaceholders()), entities);
    }

    public void send(Object entity, TextNode node, Object... entities) {
        delegate.dispatch(entity, node.getNode(), "default", ReplacePack.make(node.getPlaceholders()), entities);
    }

    public MessageHandler getDelegate() {
        return delegate;
    }
}
