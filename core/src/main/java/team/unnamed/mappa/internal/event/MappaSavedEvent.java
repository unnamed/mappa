package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapSession;

public class MappaSavedEvent extends MappaSenderEvent {
    private final MapSession sessionId;

    public MappaSavedEvent(Object sender, MapSession sessionId) {
        super(sender);
        this.sessionId = sessionId;
    }

    public MapSession getSession() {
        return sessionId;
    }
}
