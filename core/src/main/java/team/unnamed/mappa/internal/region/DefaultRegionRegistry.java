package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRegionRegistry implements RegionRegistry {
    private final Map<String, RegionFactory> factoryMap = new HashMap<>();
    private final Map<String, RegionSelection> selectionMap;

    public DefaultRegionRegistry(Map<String, RegionSelection> selectionMap) {
        this.selectionMap = selectionMap;
    }

    @Override
    public void registerRegion(String id, RegionFactory factory) {
        factoryMap.put(id, factory);
    }

    @Override
    public Region newRegion(String id, List<Vector> points) {
        return factoryMap.get(id).newRegion(points);
    }

    @Override
    public RegionSelection getSelection(String id) {
        return selectionMap.get(id);
    }
}
