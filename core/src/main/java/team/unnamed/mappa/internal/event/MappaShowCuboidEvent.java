package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.region.Cuboid;

public class MappaShowCuboidEvent extends MappaSenderEvent {
    private final MapEditSession session;
    private final Cuboid region;

    protected MappaShowCuboidEvent(Object sender, MapEditSession session, Cuboid region) {
        super(sender);
        this.session = session;
        this.region = region;
    }

    public MapEditSession getSession() {
        return session;
    }

    public Cuboid getRegion() {
        return region;
    }
}
