package team.unnamed.mappa.internal.message.placeholder;

import me.yushust.message.format.PlaceholderProvider;
import me.yushust.message.track.ContextRepository;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapEditSession;

public class MapSessionPlaceholder implements PlaceholderProvider<MapEditSession> {

    @Override
    public @Nullable Object replace(ContextRepository context,
                                    MapEditSession session,
                                    String placeholder) {
        switch (placeholder) {
            case "id":
                return session.getId();
            case "map_name":
                return session.getMapName();
            case "world_name":
                return session.getWorldName();
            case "warning":
                return session.isWarning();
            case "scheme":
                return session.getSchemeName();
        }
        return null;
    }
}
