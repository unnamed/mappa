package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Vector;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DefaultRegionRegistry implements RegionRegistry {
    protected final Map<Class<?>, RegionFactory<?>> factoryMap = new HashMap<>();
    protected final Map<String, Map<Class<?>, RegionSelection<?>>> selectionMap;

    public DefaultRegionRegistry(Map<String, Map<Class<?>, RegionSelection<?>>> selectionMap) {
        this.selectionMap = selectionMap;

        // Ah shit, here we go again
        registerRegionFactory(Vector.class, selection ->
            new Cuboid(selection.getFirstPoint(), selection.getSecondPoint()));
        registerRegionFactory(Chunk.class, selection ->
            new ChunkCuboid(selection.getFirstPoint(), selection.getSecondPoint()));
    }

    @Override
    public <T> void registerRegionFactory(Class<T> type, RegionFactory<T> factory) {
        factoryMap.put(type, factory);
    }

    @Override
    public <T> Region<T> newRegion(RegionSelection<T> selection) {
        RegionFactory<T> factory = (RegionFactory<T>) factoryMap.get(selection.getType());
        if (factory == null) {
            return null;
        }
        return factory.newRegion(selection);
    }

    @Override
    public <T> RegionSelection<T> newSelection(String id, Class<T> type) {
        RegionSelection<T> selection = RegionSelection.newSelection(type);
        selectionMap.compute(id, (key, map) -> {
            if (map == null) {
                map = new HashMap<>();
            }

            map.put(selection.getType(), selection);
            return map;
        });
        return selection;
    }

    @Override
    public <T> RegionSelection<T> getSelection(String id, Class<T> type) {
        Map<Class<?>, RegionSelection<?>> selections = selectionMap.get(id);
        if (selections == null || selections.isEmpty()) {
            return null;
        }
        return (RegionSelection<T>) selections.get(type);
    }
}
