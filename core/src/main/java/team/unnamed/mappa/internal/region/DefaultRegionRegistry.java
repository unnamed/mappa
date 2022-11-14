package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Vector;

import java.util.HashMap;
import java.util.Map;

public class DefaultRegionRegistry extends AbstractRegionRegistry {
    private final Map<String, Map<Class<?>, RegionSelection<?>>> selectionMap;

    public DefaultRegionRegistry(Map<String, Map<Class<?>, RegionSelection<?>>> selectionMap) {
        this.selectionMap = selectionMap;
        // Ah shit, here we go again
        registerRegionFactory(Vector.class, selection ->
            new Cuboid(selection.getFirstPoint(), selection.getSecondPoint()));
        registerRegionFactory(Chunk.class, selection ->
            new ChunkCuboid(selection.getFirstPoint(), selection.getSecondPoint()));
    }

    public DefaultRegionRegistry() {
        this(new HashMap<>());
    }

    @Override
    public <T> RegionSelection<T> newSelection(String id, Class<T> type) {
        RegionSelection<T> selection = RegionSelection.newSelection(type);
        Map<Class<?>, RegionSelection<?>> selections = selectionMap.get(id);
        if (selections == null) {
            selections = new HashMap<>();
            selections.put(type, selection);
            selectionMap.put(id, selections);
        } else {
            selections.put(type, selection);
        }
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
