package team.unnamed.mappa.internal.message;

import me.fixeddev.commandflow.Namespace;
import me.fixeddev.commandflow.translator.TranslationProvider;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.model.MappaPlayer;

public class MessageTranslationProvider implements TranslationProvider {

    @Override
    public String getTranslation(Namespace namespace, String node) {
        MappaPlayer entity = namespace.getObject(MappaPlayer.class, MappaCommandManager.MAPPA_PLAYER);
        return entity.format("commandflow." + node);
    }
}
