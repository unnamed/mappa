package team.unnamed.mappa.model.visualizer;

import team.unnamed.mappa.model.MappaPlayer;

public interface Render<T> {

    interface Factory<T> {

        Render<T> newRender();
    }

    void render(MappaPlayer entity, T object, int radius, boolean renovate);

    @SuppressWarnings("unchecked")
    default void renderCast(MappaPlayer entity, Object object, int radius, boolean renovate) {
        render(entity, (T) object, radius, renovate);
    }

    Class<T> getType();
}
