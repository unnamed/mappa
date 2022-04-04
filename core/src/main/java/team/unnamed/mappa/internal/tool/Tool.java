package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.object.Vector;

import java.util.List;

public interface Tool<T> {

    enum Button {
        LEFT, RIGHT, SHIFT, CONTROL
    }

    interface Action<T> {
        void call(T entity, Vector lookingAt, Button button);
    }

    static <T> Tool<T> newTool(String id, String permission, Class<T> entityType) {
        return new DefaultTool<>(id, permission, entityType);
    }

    Tool<T> registerAction(Action<T> action);

    List<Action<T>> getActions();

    void interact(T entity, Vector lookingAt, Button button);

    String getId();

    String getPermission();

    Class<T> getEntityType();
}
