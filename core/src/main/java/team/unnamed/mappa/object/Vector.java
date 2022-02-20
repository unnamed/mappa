package team.unnamed.mappa.object;

public class Vector implements Cloneable {
    protected int x;
    protected int y;
    protected int z;

    public Vector() {}

    public Vector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector remove(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Vector clone() {
        return new Vector(x, y, z);
    }
}