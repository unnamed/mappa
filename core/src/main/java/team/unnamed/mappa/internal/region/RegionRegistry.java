package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.Vector;

import java.util.Map;

public interface RegionRegistry {

    static RegionRegistry newRegistry(Map<String, Map<Class<?>, RegionSelection<?>>> selectionMap) {
        return new DefaultRegionRegistry(selectionMap);
    }

    static RegionRegistry newRegistry() {
        return new DefaultRegionRegistry();
    }

    <T> void registerRegionFactory(Class<T> type, RegionFactory<T> factory);

    <T> Region<T> newRegion(RegionSelection<T> selection);

    <T> RegionSelection<T> newSelection(String id, Class<T> type);

    default RegionSelection<Vector> newVectorSelection(String id) {
        return newSelection(id, Vector.class);
    }

    default RegionSelection<Chunk> newChunkSelection(String id) {
        return newSelection(id, Chunk.class);
    }

    <T> RegionSelection<T> getSelection(String id, Class<T> type);

    default <T> RegionSelection<T> getOrNewSelection(String id, Class<T> clazz) {
        RegionSelection<T> selection = getSelection(id, clazz);
        return selection == null ? newSelection(id, clazz) : selection;
    }

    default RegionSelection<Vector> getVectorSelection(String id) {
        return getSelection(id, Vector.class);
    }

    default RegionSelection<Vector> getOrNewVectorSelection(String id) {
        return getOrNewSelection(id, Vector.class);
    }

    default RegionSelection<Chunk> getChunkSelection(String id) {
        return getSelection(id, Chunk.class);
    }

    default RegionSelection<Chunk> getOrNewChunkSelection(String id) {
        return getOrNewSelection(id, Chunk.class);
    }
}
