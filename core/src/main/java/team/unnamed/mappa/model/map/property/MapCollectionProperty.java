package team.unnamed.mappa.model.map.property;

public interface MapCollectionProperty extends MapProperty {

    Object getValue(int slot);

    void remove(Object value);

    boolean isEmpty();
}
