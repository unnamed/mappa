package team.unnamed.mappa.model.visualizer;

public interface Render<E, T> {

    interface Factory<E, T> {

        Render<E, T> newRender();
    }

    void render(E entity, T object);

    @SuppressWarnings("unchecked")
    default void renderCast(E entity, Object object) {
        render(entity, (T) object);
    }

    Class<T> getType();
}
