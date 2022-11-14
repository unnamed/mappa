package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapSession;

public interface MapSessionEvent extends MappaEvent, MappaPlayerEvent {

    MapSession getMapSession();

    default String getMapSessionId() {
        return getMapSession().getId();
    }
}
