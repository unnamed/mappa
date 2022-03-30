package team.unnamed.mappa.model.region;

import team.unnamed.mappa.object.Vector;

public class Bounds {
    protected Vector min;
    protected Vector max;

    public Bounds(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    public Vector getMin() {
        return min;
    }

    public Vector getMax() {
        return max;
    }
}
