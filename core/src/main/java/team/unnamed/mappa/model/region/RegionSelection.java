package team.unnamed.mappa.model.region;

import team.unnamed.mappa.object.Vector;

import java.util.Set;

public interface RegionSelection {

    String setID(String regionName);

    void addPoint(Vector point);

    void removePoint(Vector point);

    Set<Vector> getPoints();

    String getID();
}
