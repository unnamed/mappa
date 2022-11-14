package team.unnamed.mappa.model;

import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.visualizer.PropertyVisual;
import team.unnamed.mappa.model.visualizer.Render;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BukkitPropertyVisual implements PropertyVisual {
    private final MapProperty property;
    private final Render<?> render;
    private final int radius;
    private final Set<MappaPlayer> viewers = new HashSet<>();

    private Object lastValue;

    public BukkitPropertyVisual(MapProperty property, Render<?> render, int radius) {
        this.property = property;
        this.render = render;
        this.radius = radius;
    }

    @Override
    public void hide(MappaPlayer entity) {
        viewers.remove(entity);
    }

    @Override
    public void show(MappaPlayer entity) {
        viewers.add(entity);
    }

    @Override
    public void render() {
        Object value = property.getValue();
        if (value == null) {
            return;
        }

        boolean renovate = lastValue != value;
        lastValue = value;
        for (MappaPlayer viewer : viewers) {
            render.renderCast(viewer, value, radius, renovate);
        }
    }

    @Override
    public Set<MappaPlayer> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    @Override
    public MapProperty getProperty() {
        return property;
    }
}
