package team.unnamed.mappa.internal.tool;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.object.Vector;

/**
 * Class of tool with interaction, permission, etc.
 */
public interface Tool {

    enum Button {LEFT, RIGHT}

    boolean canInteractWithAir();

    void interact(MappaPlayer entity, Vector lookingAt, Button button, boolean shift);

    String getId();

    String getPermission();

    Class<?> getSelectionType();
}
