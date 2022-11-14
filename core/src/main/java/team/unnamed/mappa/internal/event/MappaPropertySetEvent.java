package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;

public class MappaPropertySetEvent implements MapSessionEvent {
    private final MappaPlayer player;
    private final MapSession session;
    private final String path;
    private final MapProperty property;
    private final boolean silent;

    public MappaPropertySetEvent(MappaPlayer player,
                                 MapSession session,
                                 String path,
                                 MapProperty property,
                                 boolean silent) {
        this.player = player;
        this.session = session;
        this.path = path;
        this.property = property;
        this.silent = silent;
    }

    @Override
    public MappaPlayer getPlayer() {
        return player;
    }

    public String getPath() {
        return path;
    }

    public MapProperty getProperty() {
        return property;
    }


    @Override
    public MapSession getMapSession() {
        return session;
    }

    public boolean isSilent() {
        return silent;
    }
}
