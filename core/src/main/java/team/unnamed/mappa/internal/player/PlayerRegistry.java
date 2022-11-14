package team.unnamed.mappa.internal.player;

import me.fixeddev.commandflow.Namespace;
import team.unnamed.mappa.model.MappaPlayer;

import java.util.Collection;
import java.util.UUID;

public interface PlayerRegistry<T> {

    MappaPlayer get(T type);

    MappaPlayer get(Namespace namespace);

    MappaPlayer get(UUID uuid);

    MappaPlayer console();

    Collection<MappaPlayer> all();

    void invalidate(MappaPlayer player);

    void invalidate(UUID uuid);

    void invalidateConsole();

    void invalidateAll();
}
