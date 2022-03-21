package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.object.Vector;

import java.util.List;

public interface RegionFactory {

    Region newRegion(List<Vector> points);
}
