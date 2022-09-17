package team.unnamed.mappa.internal;

public interface Repository<T> {

    T get(String id) throws Exception;

    void put(String id, T object) throws Exception;

    boolean contains(String id) throws Exception;

    T remove(String id) throws Exception;
}
