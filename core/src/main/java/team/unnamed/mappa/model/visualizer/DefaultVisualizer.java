package team.unnamed.mappa.model.visualizer;

import team.unnamed.mappa.model.BukkitPropertyVisual;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.Key;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.region.RegionSelection;

import java.lang.reflect.Type;
import java.util.*;

public class DefaultVisualizer implements Visualizer {
    public static final int VISUAL_RADIUS = 15;
    public static final int MAX_VISUALS = 10;
    protected Map<Type, Render.Factory<?>> typeVisuals = new HashMap<>();

    public static Key<Map<String, PropertyVisual>> VISUALS = new Key<>("");

    protected Map<String, Map<String, PropertyVisual>> sessionVisuals = new HashMap<>();
    protected Map<UUID, Set<PropertyVisual>> entityVisuals = new HashMap<>();
    protected Map<UUID, Visual> selectionVisuals = new HashMap<>();

    @Override
    public Map<String, PropertyVisual> createVisuals(MapSession session) {
        Map<String, PropertyVisual> visualMap = session.getObject(
            VISUALS, s -> new LinkedHashMap<>());
        for (String path : session.getObject(MapScheme.PLAIN_KEYS)) {
            MapProperty property = session.getProperty(path);
            PropertyVisual visual = createVisual(property);
            if (visual == null) {
                continue;
            }

            visualMap.put(path, visual);
        }
        sessionVisuals.put(session.getId(), visualMap);
        return visualMap;
    }

    @Override
    public Visual createVisual(MappaPlayer entity, RegionSelection<?> selection) {
        Render.Factory<?> factory = getRenderFactoryOf(selection.getType());
        Visual visual;
        if (factory == null) {
            visual = null;
        } else {
            Render<?> render = factory.newRender();
            visual = () -> {
                Object firstPoint = selection.getFirstPoint();
                if (firstPoint != null) {
                    render.renderCast(entity, firstPoint, VISUAL_RADIUS, true);
                }
                Object secondPoint = selection.getSecondPoint();
                if (secondPoint != null) {
                    render.renderCast(entity, secondPoint, VISUAL_RADIUS, true);
                }
            };
        }
        if (visual != null) {
            selectionVisuals.put(entity.getUniqueId(), visual);
        }
        return visual;
    }

    @Override
    public PropertyVisual createVisual(MapProperty property) {
        Render.Factory<?> factory = getRenderFactoryOf(property.getType());
        return factory == null
            ? null
            : new BukkitPropertyVisual(property, factory.newRender(), VISUAL_RADIUS);
    }

    @Override
    public boolean hasVisuals(UUID uuid) {
        return entityVisuals.containsKey(uuid);
    }

    @Override
    public Set<PropertyVisual> getVisualsOf(UUID uuid) {
        return entityVisuals.computeIfAbsent(uuid, key -> new LinkedHashSet<>());
    }

    @Override
    public Set<PropertyVisual> clearVisualsOf(UUID uuid) {
        return entityVisuals.remove(uuid);
    }

    @Override
    public void clearSelectionVisualOf(UUID uuid) {
        selectionVisuals.remove(uuid);
    }

    @Override
    public Visual getSelectionVisualOf(UUID uuid) {
        return selectionVisuals.get(uuid);
    }

    @Override
    public Map<String, PropertyVisual> getVisualsOfSession(MapSession session) {
        return sessionVisuals.get(session.getId());
    }

    @Override
    public PropertyVisual getPropertyVisualOf(MapSession session, String path) {
        return getVisualsOfSession(session).get(path);
    }

    public Map<String, Map<String, PropertyVisual>> getSessionVisuals() {
        return sessionVisuals;
    }

    @Override
    public Map<UUID, Set<PropertyVisual>> getEntityVisuals() {
        return entityVisuals;
    }

    @Override
    public Map<UUID, Visual> getSelectionVisuals() {
        return selectionVisuals;
    }

    @Override
    public void unregisterAll() {
        Collection<Map<String, PropertyVisual>> sessions = sessionVisuals.values();
        for (Map<String, PropertyVisual> session : sessions) {
            Iterator<Map.Entry<String, PropertyVisual>> iterator = session.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PropertyVisual> next = iterator.next();
                PropertyVisual value = next.getValue();
                value.clear();
                iterator.remove();
            }
        }

        Iterator<Map.Entry<UUID, Set<PropertyVisual>>> entityIt = entityVisuals.entrySet().iterator();
        while (entityIt.hasNext()) {
            Map.Entry<UUID, Set<PropertyVisual>> next = entityIt.next();
            Set<PropertyVisual> list = next.getValue();
            list.forEach(PropertyVisual::clear);
            entityIt.remove();
        }

        selectionVisuals = new HashMap<>();
        typeVisuals = new HashMap<>();
    }

    @Override
    public <T> void registerVisual(Class<T> type, Render.Factory<T> render) {
        typeVisuals.put(type, render);
    }

    @Override
    public void unregisterVisual(Type type) {
        typeVisuals.remove(type);
    }

    @Override
    public Render.Factory<?> getRenderFactoryOf(Type type) {
        return typeVisuals.get(type);
    }

    @Override
    public Map<Type, Render.Factory<?>> getTypeVisuals() {
        return Collections.unmodifiableMap(typeVisuals);
    }

    @Override
    public int getMaxVisuals() {
        return MAX_VISUALS;
    }

}
