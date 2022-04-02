package team.unnamed.mappa.model.region;

public class DefaultRegionSelection<T> implements RegionSelection<T> {
    protected Class<T> type;

    protected T first;
    protected T second;

    @Override
    public void setFirstPoint(T vector) {
        this.first = vector;
    }

    @Override
    public void setSecondPoint(T vector) {
        this.second = vector;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T getFirstPoint() {
        return first;
    }

    @Override
    public T getSecondPoint() {
        return second;
    }
}
