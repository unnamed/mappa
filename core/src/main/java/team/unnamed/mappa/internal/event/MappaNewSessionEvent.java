package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;

public class MappaNewSessionEvent implements MapSessionEvent {
    public enum Reason {RESUMED, CREATED}

    private final MappaPlayer player;
    private final MapSession session;
    private final Reason reason;

    public MappaNewSessionEvent(MappaPlayer player, MapSession session, Reason reason) {
        this.player = player;
        this.session = session;
        this.reason = reason;
    }

    @Override
    public MappaPlayer getPlayer() {
        return player;
    }

    @Override
    public MapSession getMapSession() {
        return session;
    }

    public Reason getReason() {
        return reason;
    }
}
