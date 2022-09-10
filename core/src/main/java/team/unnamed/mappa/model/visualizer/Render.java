package team.unnamed.mappa.model.visualizer;

public interface Render<E, T> {

    interface Factory<E, T> {

        Render<E, T> newRender();
    }

    void render(E entity, T object, boolean newTick);

    @SuppressWarnings("unchecked")
    default void renderCast(E entity, Object object, boolean newTick) {
        render(entity, (T) object, newTick);
    }

    Class<T> getType();
}
