package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.Text;

import java.util.List;

public class MappaPropertySetEvent implements MapSessionEvent {
    private final Object entity;
    private final MapSession session;
    private final String path;
    private final List<Text> translations;
    private final MapProperty property;
    private final boolean silent;

    public MappaPropertySetEvent(Object entity,
                                 MapSession session,
                                 String path,
                                 MapProperty property,
                                 List<Text> translations) {
        this(entity, session, path, translations, property, false);
    }

    public MappaPropertySetEvent(Object entity,
                                 MapSession session,
                                 String path,
                                 List<Text> translations,
                                 MapProperty property,
                                 boolean silent) {
        this.entity = entity;
        this.session = session;
        this.path = path;
        this.translations = translations;
        this.property = property;
        this.silent = silent;
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

    public List<Text> getMessages() {
        return translations;
    }

    @Override
    public MapSession getMapSession() {
        return session;
    }

    public boolean isSilent() {
        return silent;
    }
}
