package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.object.Vector;

public abstract class AbstractTool<T> implements Tool<T> {
    protected final String id;
    protected final String permission;
    protected final boolean interactAir;
    protected final Class<T> entityType;
    protected final Class<?> selectionType;

    public AbstractTool(String id,
                        String permissionGroup,
                        boolean interactAir,
                        Class<T> entityType,
                        Class<?> selectionType) {
        this.id = id;
        this.selectionType = selectionType;
        this.permission = permissionGroup + "." + id;
        this.interactAir = interactAir;
        this.entityType = entityType;
    }

    @Override
    public boolean canInteractWithAir() {
        return interactAir;
    }

    @Override
    public abstract void interact(T entity, Vector lookingAt, Button button, boolean shift);

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

    @Override
    public Class<?> getSelectionType() {
        return selectionType;
    }
}
