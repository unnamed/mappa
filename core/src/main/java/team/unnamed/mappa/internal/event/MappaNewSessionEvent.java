package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapSession;

public class MappaNewSessionEvent implements MapSessionEvent {
    public enum Reason {RESUMED, CREATED}

    private final MapSession session;
    private final Reason reason;

    public MappaNewSessionEvent(MapSession session, Reason reason) {
        this.session = session;
        this.reason = reason;
    }

    @Override
    public MapSession getMapSession() {
        return session;
    }

    public Reason getReason() {
        return reason;
    }
}
