package team.unnamed.mappa.model.visualizer;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.region.RegionSelection;

import java.lang.reflect.Type;
import java.util.Map;

public interface Visualizer<E> {

    <T> void registerVisual(Class<T> type, Render.Factory<E, T> render);

    Map<String, PropertyVisual<E>> createVisuals(MapEditSession session);

    @Nullable
    Visual createVisual(E entity, RegionSelection<?> selection);

    PropertyVisual<E> createVisual(MapProperty property);

    void unregisterVisual(Type type);

    void unregisterAll();

    @SuppressWarnings("unchecked")
    default <T> Render.Factory<E, T> getRenderFactoryOf(Class<T> type) {
        return (Render.Factory<E, T>) getRenderFactoryOf((Type) type);
    }

    Render.Factory<E, ?> getRenderFactoryOf(Type type);

    Map<Type, Render.Factory<E, ?>> getTypeVisuals();

}
