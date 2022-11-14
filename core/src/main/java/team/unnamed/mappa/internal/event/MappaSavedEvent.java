package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;

public class MappaSavedEvent extends MappaSenderEvent implements MapSessionEvent {
    private final MapSession session;

    public MappaSavedEvent(MappaPlayer player, MapSession session) {
        super(player);
        this.session = session;
    }

    @Override
    public MapSession getMapSession() {
        return session;
    }
}
