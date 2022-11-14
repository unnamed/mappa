package team.unnamed.mappa.model.region;

public interface RegionSelection<T> {

    enum Order {
        FIRST, LAST
    }

    static <T> RegionSelection<T> newSelection(Class<T> type) {
        return new DefaultRegionSelection<>(type);
    }

    void setFirstPoint(T first);

    void setSecondPoint(T second);

    default void clearPoints() {
        setFirstPoint(null);
        setSecondPoint(null);
    }

    Class<T> getType();

    T getFirstPoint();

    T getSecondPoint();
}
