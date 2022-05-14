package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.object.Vector;

import java.util.List;

public interface Tool<T> {

    enum Button {
        LEFT, RIGHT
    }

    interface Action<T> {
        void call(T entity, Vector lookingAt, Button button, boolean shift);
    }

    static <T> Tool<T> newTool(String id,
                               String permission,
                               boolean interactAir,
                               Class<T> entityType) {
        return new DefaultTool<>(id, permission, interactAir, entityType);
    }

    Tool<T> registerAction(Action<T> action);

    boolean canInteractWithAir();

    List<Action<T>> getActions();

    void interact(T entity, Vector lookingAt, Button button, boolean shift);

    String getId();

    String getPermission();

    Class<T> getEntityType();
}
