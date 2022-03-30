package team.unnamed.mappa.model.region;

import team.unnamed.mappa.object.Vector;

public interface Region {

    Vector getMinimum();

    Vector getMaximum();

    boolean contains(Vector vector);
}
