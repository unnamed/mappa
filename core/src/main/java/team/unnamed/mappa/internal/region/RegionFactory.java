package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.model.region.RegionSelection;

public interface RegionFactory<T> {

    Region<T> newRegion(RegionSelection<T> selection);
}
