package team.unnamed.mappa.internal.message;

import me.fixeddev.commandflow.Namespace;
import me.fixeddev.commandflow.translator.TranslationProvider;
import team.unnamed.mappa.function.EntityProvider;

public class MessageTranslationProvider implements TranslationProvider {
    private final String parentNode;
    private final MappaTextHandler textHandler;
    private final EntityProvider entityProvider;

    public MessageTranslationProvider(String parentNode,
                                      MappaTextHandler textHandler,
                                      EntityProvider entityProvider) {
        this.parentNode = parentNode;
        this.textHandler = textHandler;
        this.entityProvider = entityProvider;
    }

    @Override
    public String getTranslation(Namespace namespace, String node) {
        Object entity = entityProvider.from(namespace);
        return textHandler.format(entity, parentNode + node);
    }
}
