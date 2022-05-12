package team.unnamed.mappa.model.region;

public interface RegionSelection<T> {

    static <T> RegionSelection<T> newSelection(Class<T> type) {
        return new DefaultRegionSelection<>(type);
    }

    void setFirstPoint(T vector);

    void setSecondPoint(T vector);

    default void clearPoints() {
        setFirstPoint(null);
        setSecondPoint(null);
    }

    Class<T> getType();

    T getFirstPoint();

    T getSecondPoint();
}
