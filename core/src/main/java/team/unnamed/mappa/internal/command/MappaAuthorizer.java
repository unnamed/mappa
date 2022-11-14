package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.Authorizer;
import me.fixeddev.commandflow.Namespace;
import team.unnamed.mappa.internal.player.PlayerRegistry;
import team.unnamed.mappa.model.MappaPlayer;

public class MappaAuthorizer implements Authorizer {
    private final Authorizer delegate;
    private final PlayerRegistry<?> registry;

    public MappaAuthorizer(Authorizer delegate, PlayerRegistry<?> registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @Override
    public boolean isAuthorized(Namespace namespace, String perm) {
        boolean result = delegate.isAuthorized(namespace, perm);
        if (result) {
            // Dirty trick to set object before
            // part parsing and avoid exceptions
            // with MappaPlayerPart and more.
            namespace.setObject(MappaPlayer.class,
                MappaCommandManager.MAPPA_PLAYER,
                registry.get(namespace));
        }
        return result;
    }
}
