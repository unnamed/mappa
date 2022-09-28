package team.unnamed.mappa.model.visualizer;

public interface Render<E, T> {

    interface Factory<E, T> {

        Render<E, T> newRender();
    }

    void render(E entity, T object, int radius, boolean renovate);

    @SuppressWarnings("unchecked")
    default void renderCast(E entity, Object object, int radius, boolean renovate) {
        render(entity, (T) object, radius, renovate);
    }

    Class<T> getType();
}
