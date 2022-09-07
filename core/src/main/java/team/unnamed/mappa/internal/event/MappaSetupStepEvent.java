package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapEditSession;

public class MappaSetupStepEvent extends MappaSenderEvent {
    private final MapEditSession session;

    public MappaSetupStepEvent(Object sender, MapEditSession session) {
        super(sender);
        this.session = session;
    }

    public MapEditSession getSession() {
        return session;
    }
}
