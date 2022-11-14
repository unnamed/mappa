package team.unnamed.mappa.model.visualizer;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.property.MapProperty;

import java.util.Set;

public interface PropertyVisual extends Visual{

    void hide(MappaPlayer entity);

    void show(MappaPlayer entity);

    default void clear() {
        getViewers().forEach(this::hide);
    }

    Set<MappaPlayer> getViewers();

    MapProperty getProperty();
}
