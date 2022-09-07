package team.unnamed.mappa.model.visualizer;

import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.region.RegionSelection;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractVisualizer<E> implements Visualizer<E> {
    protected Map<Type, Render.Factory<E, ?>> typeVisuals = new HashMap<>();

    @Override
    public <T> void registerVisual(Class<T> type, Render.Factory<E, T> render) {
        typeVisuals.put(type, render);
    }

    @Override
    public void unregisterVisual(Type type) {
        typeVisuals.remove(type);
    }

    @Override
    public void unregisterAll() {
        typeVisuals = new HashMap<>();
    }

    @Override
    public Render.Factory<E, ?> getRenderFactoryOf(Type type) {
        return typeVisuals.get(type);
    }

    @Override
    public Map<Type, Render.Factory<E, ?>> getTypeVisuals() {
        return Collections.unmodifiableMap(typeVisuals);
    }

    @Override
    public abstract Map<String, PropertyVisual<E>> createVisuals(MapEditSession session);

    @Override
    public abstract Visual createVisual(E entity, RegionSelection<?> selection);

    @Override
    public abstract PropertyVisual<E> createVisual(MapProperty property);
}
