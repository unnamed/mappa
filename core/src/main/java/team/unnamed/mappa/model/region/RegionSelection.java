package team.unnamed.mappa.model.region;

import team.unnamed.mappa.object.Vector;

public interface RegionSelection {

    void setID(String regionId);

    void setFirstVector(Vector vector);

    void setSecondVector(Vector vector);

    Vector getFirstVector();

    Vector getSecondVector();

    String getID();
}
