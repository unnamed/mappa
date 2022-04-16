package team.unnamed.mappa.bukkit.internal;

import com.google.common.cache.Cache;
import team.unnamed.mappa.internal.region.AbstractRegionRegistry;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Vector;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class CacheRegionRegistry extends AbstractRegionRegistry {
    private final Cache<String, Map<Class<?>, RegionSelection<?>>> cacheMap;

    public CacheRegionRegistry(Cache<String, Map<Class<?>, RegionSelection<?>>> cacheMap) {
        this.cacheMap = cacheMap;

        // Ah shit, here we go again
        registerRegionFactory(Vector.class, selection ->
            new Cuboid(selection.getFirstPoint(), selection.getSecondPoint()));
        registerRegionFactory(Chunk.class, selection ->
            new ChunkCuboid(selection.getFirstPoint(), selection.getSecondPoint()));
    }

    @Override
    public <T> RegionSelection<T> newSelection(String id, Class<T> type) {
        RegionSelection<T> selection = RegionSelection.newSelection(type);
        Map<Class<?>, RegionSelection<?>> selections = cacheMap.getIfPresent(id);
        if (selections == null) {
            selections = new HashMap<>();
            selections.put(type, selection);
            cacheMap.put(id, selections);
        } else {
            selections.put(type, selection);
        }
        return selection;
    }

    @Override
    public <T> RegionSelection<T> getSelection(String id, Class<T> type) {
        Map<Class<?>, RegionSelection<?>> selections = cacheMap.getIfPresent(id);
        if (selections == null || selections.isEmpty()) {
            return null;
        }
        return (RegionSelection<T>) selections.get(type);
    }
}
