package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;

public class MappaSetupStepEvent extends MappaSenderEvent {
    private final MapSession session;

    public MappaSetupStepEvent(MappaPlayer sender, MapSession session) {
        super(sender);
        this.session = session;
    }

    public MapSession getSession() {
        return session;
    }
}
