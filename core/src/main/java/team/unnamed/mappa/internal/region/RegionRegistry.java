package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Vector;

import java.util.List;
import java.util.Map;

public interface RegionRegistry {

    static RegionRegistry newRegistry(Map<String, RegionSelection> selectionMap) {
        return new DefaultRegionRegistry(selectionMap);
    }

    void registerRegion(String id, RegionFactory factory);

    Region newRegion(String id, List<Vector> points);

    RegionSelection getSelection(String id);
}
