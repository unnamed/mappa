package team.unnamed.mappa.model.region;

import team.unnamed.mappa.object.Vector;

import java.util.Set;

public interface RegionSelection {

    String setRegionId(String regionName);

    void addPoint(Vector point);

    void removePoint(Vector point);

    Set<Vector> getPoints();

    String getRegionId();
}
