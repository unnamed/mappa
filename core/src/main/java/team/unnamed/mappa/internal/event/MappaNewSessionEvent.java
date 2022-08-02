package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapSession;

public class MappaNewSessionEvent implements MapSessionEvent {
    private final MapSession session;

    public MappaNewSessionEvent(MapSession session) {
        this.session = session;
    }

    @Override
    public MapSession getMapSession() {
        return session;
    }
}
