package team.unnamed.mappa.bukkit.internal;

import org.bukkit.entity.Player;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.Key;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.model.visualizer.AbstractVisualizer;
import team.unnamed.mappa.model.visualizer.PropertyVisual;
import team.unnamed.mappa.model.visualizer.Render;
import team.unnamed.mappa.model.visualizer.Visual;

import java.util.*;

public class BukkitVisualizer extends AbstractVisualizer<Player> {
    public static Key<Map<String, PropertyVisual<Player>>> VISUALS = new Key<>("");

    protected Map<String, Map<String, PropertyVisual<Player>>> sessionVisuals = new HashMap<>();
    protected Map<UUID, Set<PropertyVisual<Player>>> entityVisuals = new HashMap<>();
    protected Map<UUID, Visual> selectionVisual = new HashMap<>();

    @Override
    public Map<String, PropertyVisual<Player>> createVisuals(MapEditSession session) {
        Map<String, PropertyVisual<Player>> visualMap = session.getObject(
            VISUALS, s -> new LinkedHashMap<>());
        for (String path : session.getObject(MapScheme.PLAIN_KEYS)) {
            MapProperty property = session.getProperty(path);
            PropertyVisual<Player> visual = createVisual(property);
            if (visual == null) {
                continue;
            }

            visualMap.put(path, visual);
        }
        sessionVisuals.put(session.getId(), visualMap);
        return visualMap;
    }

    @Override
    public Visual createVisual(Player entity, RegionSelection<?> selection) {
        Render.Factory<Player, ?> factory = getRenderFactoryOf(selection.getType());
        Visual visual;
        if (factory == null) {
            visual = null;
        } else {
            Render<Player, ?> render = factory.newRender();
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
            selectionVisual.put(entity.getUniqueId(), visual);
        }
        return visual;
    }

    @Override
    public PropertyVisual<Player> createVisual(MapProperty property) {
        Render.Factory<Player, ?> factory = getRenderFactoryOf(property.getType());
        return factory == null
            ? null
            : new BukkitPropertyVisual(property, factory.newRender(), VISUAL_RADIUS);
    }

    public boolean hasVisuals(Player player) {
        return hasVisuals(player.getUniqueId());
    }

    public boolean hasVisuals(UUID uuid) {
        return entityVisuals.containsKey(uuid);
    }

    public Set<PropertyVisual<Player>> getVisualsOf(Player player) {
        return getVisualsOf(player.getUniqueId());
    }

    public Set<PropertyVisual<Player>> getVisualsOf(UUID uuid) {
        return entityVisuals.computeIfAbsent(uuid, key -> new LinkedHashSet<>());
    }

    public void clearVisualsOf(Player player) {
        clearVisualsOf(player.getUniqueId());
    }

    public void clearVisualsOf(UUID uuid) {
        entityVisuals.remove(uuid);
    }

    public Visual getSelectionVisualOf(Player player) {
        return getSelectionVisualOf(player.getUniqueId());
    }

    public Visual getSelectionVisualOf(UUID uuid) {
        return selectionVisual.get(uuid);
    }

    public Map<String, PropertyVisual<Player>> getVisualsOfSession(MapSession session) {
        return sessionVisuals.get(session.getId());
    }

    public Map<String, Map<String, PropertyVisual<Player>>> getSessionVisuals() {
        return sessionVisuals;
    }

    public Map<UUID, Set<PropertyVisual<Player>>> getEntityVisuals() {
        return entityVisuals;
    }

    public Map<UUID, Visual> getSelectionVisual() {
        return selectionVisual;
    }

    @Override
    public void unregisterAll() {
        Collection<Map<String, PropertyVisual<Player>>> sessions = sessionVisuals.values();
        for (Map<String, PropertyVisual<Player>> session : sessions) {
            Iterator<Map.Entry<String, PropertyVisual<Player>>> iterator = session.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PropertyVisual<Player>> next = iterator.next();
                PropertyVisual<Player> value = next.getValue();
                value.clear();
                iterator.remove();
            }
        }

        Iterator<Map.Entry<UUID, Set<PropertyVisual<Player>>>> entityIt = entityVisuals.entrySet().iterator();
        while (entityIt.hasNext()) {
            Map.Entry<UUID, Set<PropertyVisual<Player>>> next = entityIt.next();
            Set<PropertyVisual<Player>> list = next.getValue();
            list.forEach(PropertyVisual::clear);
            entityIt.remove();
        }

        selectionVisual = new HashMap<>();
        super.unregisterAll();
    }
}
