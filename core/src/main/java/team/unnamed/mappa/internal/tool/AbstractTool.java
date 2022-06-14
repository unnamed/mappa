package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.object.Vector;

public abstract class AbstractTool<T> implements Tool<T> {
    protected final String id;
    protected final String permission;
    protected final boolean interactAir;
    protected final Class<T> entityType;

    public AbstractTool(String id,
                        String permissionGroup,
                        boolean interactAir,
                        Class<T> entityType) {
        this.id = id;
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
}
