package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.object.Vector;

import java.util.ArrayList;
import java.util.List;

public class DefaultTool<T> implements Tool<T> {
    private final String id;
    private final String permission;
    private final boolean interactAir;
    private final Class<T> entityType;
    private final List<Action<T>> actions = new ArrayList<>();

    public DefaultTool(String id,
                       String permission,
                       boolean interactAir,
                       Class<T> entityType) {
        this.id = id;
        this.permission = permission;
        this.interactAir = interactAir;
        this.entityType = entityType;
    }

    @Override
    public Tool<T> registerAction(Action<T> action) {
        actions.add(action);
        return this;
    }

    @Override
    public List<Action<T>> getActions() {
        return actions;
    }

    @Override
    public boolean canInteractWithAir() {
        return interactAir;
    }

    @Override
    public void interact(T entity, Vector lookingAt, Button button, boolean shift) {
        actions.forEach(action -> action.call(entity, lookingAt, button, shift));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }
}
