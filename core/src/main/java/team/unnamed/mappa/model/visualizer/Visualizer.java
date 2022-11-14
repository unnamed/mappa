package team.unnamed.mappa.model.visualizer;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.region.RegionSelection;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Visualizer {

    <T> void registerVisual(Class<T> type, Render.Factory<T> render);

    Map<String, PropertyVisual> createVisuals(MapSession session);

    @Nullable
    Visual createVisual(MappaPlayer entity, RegionSelection<?> selection);

    PropertyVisual createVisual(MapProperty property);

    void unregisterVisual(Type type);

    boolean hasVisuals(UUID uuid);

    Set<PropertyVisual> getVisualsOf(UUID uuid);

    Set<PropertyVisual> clearVisualsOf(UUID uuid);

    void clearSelectionVisualOf(UUID uuid);

    Visual getSelectionVisualOf(UUID uuid);

    default boolean hasVisuals(MappaPlayer player) {
        return hasVisuals(player.getUniqueId());
    }

    default Set<PropertyVisual> getVisualsOf(MappaPlayer player) {
        return getVisualsOf(player.getUniqueId());
    }

    default Set<PropertyVisual> clearVisualsOf(MappaPlayer player) {
        return clearVisualsOf(player.getUniqueId());
    }

    default void clearSelectionVisualOf(MappaPlayer player) {
        clearSelectionVisualOf(player.getUniqueId());
    }

    default void clearAllVisuals(MappaPlayer player) {
        Set<PropertyVisual> visuals = clearVisualsOf(player);
        if (visuals != null) {
            for (PropertyVisual visual : visuals) {
                visual.hide(player);
            }
        }
        clearSelectionVisualOf(player);
    }


    default Visual getSelectionVisualOf(MappaPlayer player) {
        return getSelectionVisualOf(player.getUniqueId());
    }

    Map<String, PropertyVisual> getVisualsOfSession(MapSession session);

    PropertyVisual getPropertyVisualOf(MapSession session, String path);

    Map<UUID, Visual> getSelectionVisuals();

    void unregisterAll();

    @SuppressWarnings("unchecked")
    default <T> Render.Factory<T> getRenderFactoryOf(Class<T> type) {
        return (Render.Factory<T>) getRenderFactoryOf((Type) type);
    }

    Render.Factory<?> getRenderFactoryOf(Type type);

    Map<Type, Render.Factory<?>> getTypeVisuals();

    int getMaxVisuals();

    Map<UUID, Set<PropertyVisual>> getEntityVisuals();
}
