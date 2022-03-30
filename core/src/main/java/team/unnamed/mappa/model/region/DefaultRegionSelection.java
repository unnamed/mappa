package team.unnamed.mappa.model.region;

import team.unnamed.mappa.object.Vector;

public class DefaultRegionSelection implements RegionSelection {
    protected String id;
    protected Vector first;
    protected Vector second;

    @Override
    public void setID(String regionId) {
        this.id = regionId;
    }

    @Override
    public void setFirstVector(Vector vector) {
        this.first = vector;
    }

    @Override
    public void setSecondVector(Vector vector) {
        this.second = vector;
    }

    @Override
    public Vector getFirstVector() {
        return first;
    }

    @Override
    public Vector getSecondVector() {
        return second;
    }

    @Override
    public String getID() {
        return id;
    }
}
