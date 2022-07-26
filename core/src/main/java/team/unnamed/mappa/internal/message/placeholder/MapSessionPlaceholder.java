package team.unnamed.mappa.internal.message.placeholder;

import me.yushust.message.format.PlaceholderProvider;
import me.yushust.message.track.ContextRepository;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.TranslationNode;

public class MapSessionPlaceholder implements PlaceholderProvider<MapSession> {

    @Override
    public @Nullable Object replace(ContextRepository context,
                                    MapSession session,
                                    String placeholder) {
        switch (placeholder) {
            case "id":
                return session.getId();
            case "warning":
                return session.isWarning();
            case "scheme":
                return session.getSchemeName();
        }
        if (session instanceof MapEditSession) {
            MapEditSession editSession = (MapEditSession) session;
            switch (placeholder) {
                case "map_name":
                    return editSession.getMapName();
                case "world_name":
                    return editSession.getWorldName();
            }
        } else if (session instanceof MapSerializedSession) {
            MapSerializedSession serializedSession = (MapSerializedSession) session;
            if ("reason".equals(placeholder)) {
                TranslationNode node = serializedSession.getReason().asTextNode();
                return context.get(context.getLanguage(), node.getNode());
            }
        }
        return null;
    }
}
