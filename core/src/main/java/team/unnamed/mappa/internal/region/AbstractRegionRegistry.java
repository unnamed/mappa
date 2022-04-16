package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.model.region.RegionSelection;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class AbstractRegionRegistry implements RegionRegistry {
    protected final Map<Class<?>, RegionFactory<?>> factoryMap = new HashMap<>();

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
}
