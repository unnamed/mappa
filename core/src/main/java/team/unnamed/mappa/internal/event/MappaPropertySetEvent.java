package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;

public class MappaPropertySetEvent implements MapSessionEvent {
    private final Object entity;
    private final MapSession session;
    private final String path;
    private final MapProperty property;

    public MappaPropertySetEvent(Object entity, MapSession session, String path, MapProperty property) {
        this.entity = entity;
        this.session = session;
        this.path = path;
        this.property = property;
    }

    public Object getEntity() {
        return entity;
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
}
