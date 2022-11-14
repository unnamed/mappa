package team.unnamed.mappa.internal;

import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;

import java.util.Collection;

public interface MapRegistry {

    void registerMapSession(MapSession session);

    default void unregisterMapSession(MapSession session) {
        unregisterMapSession(session.getId());
    }

    void unregisterMapSession(String id);

    void registerMapScheme(MapScheme scheme);

    default void unregisterMapScheme(MapScheme scheme) {
        unregisterMapSession(scheme.getName());
    }

    void unregisterMapScheme(String id);

    void unregisterAll();

    boolean containsMapSessionId(String id);

    boolean containsMapSchemeId(String id);

    MapSession getMapSession(String id);

    MapScheme getMapScheme(String id);

    Collection<MapSession> getMapSessions();

    Collection<MapScheme> getMapSchemes();
}
