package team.unnamed.mappa.internal.player;

import team.unnamed.mappa.model.MappaPlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class DefaultPlayerRegistry<T> implements PlayerRegistry<T> {
    protected final Supplier<MappaPlayer> lazyConsole;
    protected MappaPlayer console;
    protected final Map<UUID, MappaPlayer> registry = new HashMap<>();

    protected DefaultPlayerRegistry(Supplier<MappaPlayer> lazyConsole) {
        this.lazyConsole = lazyConsole;
    }

    @Override
    public MappaPlayer get(UUID uuid) {
        return registry.get(uuid);
    }

    @Override
    public MappaPlayer console() {
        return console == null ? console = lazyConsole.get() : console;
    }

    @Override
    public Collection<MappaPlayer> all() {
        return registry.values();
    }

    @Override
    public void invalidate(MappaPlayer player) {
        registry.remove(player.getUniqueId());
        player.flush();
    }

    @Override
    public void invalidate(UUID uuid) {
        MappaPlayer remove = registry.remove(uuid);
        if (remove == null) {
            return;
        }

        remove.flush();
    }

    @Override
    public void invalidateConsole() {
        this.console = null;
    }

    @Override
    public void invalidateAll() {
        registry.clear();
        invalidateConsole();
    }
}
