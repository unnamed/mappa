package team.unnamed.mappa.internal.message.placeholder;

import me.yushust.message.format.PlaceholderProvider;
import me.yushust.message.track.ContextRepository;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapSession;

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
            case "date":
                return session.getDate();
            case "map_name":
                return session.getMapName();
            case "world_name":
                return session.getWorldName();
            case "version":
                return session.getVersion();
        }
        return null;
    }
}
