package team.unnamed.mappa.model.region;

public interface Region<T> {

    T getMinimum();

    T getMaximum();

    boolean contains(T object);
}
