package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.object.Vector;

import java.util.ArrayList;
import java.util.List;

public class DefaultTool<T> implements Tool<T> {
    private final String id;
    private final Class<T> entityType;
    private final List<Action<T>> actions = new ArrayList<>();

    public DefaultTool(String id, Class<T> entityType) {
        this.id = id;
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
    public void interact(T entity, Vector lookingAt, Button button) {
        actions.forEach(action -> action.call(entity, lookingAt, button));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }
}
