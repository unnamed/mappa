package team.unnamed.mappa.internal;

import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.object.Vector;

import java.util.List;

public interface RegionRegistry {

    void registerRegion(String id, RegionFactory factory);

    Region newRegion(String id, List<Vector> points);


}
