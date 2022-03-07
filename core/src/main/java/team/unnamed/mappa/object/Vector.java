package team.unnamed.mappa.object;

public class Vector implements Cloneable {
    protected int x;
    protected int y;
    protected int z;

    protected double yaw;
    protected double pitch;

    public Vector() {
    }

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

    public Vector add(int x, int y, int z, double yaw, double pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
        return add(x, y, z);
    }

    public Vector remove(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector remove(int x, int y, int z, double yaw, double pitch) {
        this.yaw -= yaw;
        this.pitch -= pitch;
        return remove(x, y, z);
    }

    public Vector set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector set(int x, int y, int z, double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return set(x, y, z);
    }

    public Vector removeYawPitch() {
        this.yaw = 0;
        this.pitch = 0;
        return this;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Vector clone() {
        return new Vector(x, y, z);
    }
}