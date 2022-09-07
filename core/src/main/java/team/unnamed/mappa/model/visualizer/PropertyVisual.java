package team.unnamed.mappa.model.visualizer;

import team.unnamed.mappa.model.map.property.MapProperty;

import java.util.Set;

public interface PropertyVisual<T> extends Visual{

    void hide(T entity);

    void show(T entity);

    default void clear() {
        getViewers().forEach(this::hide);
    }

    Set<T> getViewers();

    MapProperty getProperty();
}
