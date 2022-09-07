package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.object.Vector;

public interface Tool<T> {

    enum Button {LEFT, RIGHT}

    boolean canInteractWithAir();

    void interact(T entity, Vector lookingAt, Button button, boolean shift);

    String getId();

    String getPermission();

    Class<T> getEntityType();

    Class<?> getSelectionType();
}
