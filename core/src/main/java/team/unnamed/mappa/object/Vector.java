package team.unnamed.mappa.object;

public class Vector implements Cloneable, Deserializable {
    protected double x;
    protected double y;
    protected double z;

    protected double yaw;
    protected double pitch;

    public static Vector getMinimum(Vector v1, Vector v2) {
        return new Vector(
            Math.min(v1.getX(), v2.getX()),
            Math.min(v1.getY(), v2.getY()),
            Math.min(v1.getZ(), v2.getZ()));
    }

    public static Vector getMaximum(Vector v1, Vector v2) {
        return new Vector(
            Math.max(v1.getX(), v2.getX()),
            Math.max(v1.getY(), v2.getY()),
            Math.max(v1.getZ(), v2.getZ()));
    }

    public static boolean isInAABB(Vector point, Vector min, Vector max) {
        return point.getX() >= min.getX()
            && point.getY() >= min.getY()
            && point.getZ() >= min.getZ()
            && point.getX() <= max.getX()
            && point.getY() <= max.getY()
            && point.getZ() <= max.getZ();
    }

    public static Vector fromString(String line) {
        String[] split = line.split(",");
        if (split.length < 3) {
            throw new IllegalArgumentException("Insufficient arguments for vector");
        }
        double[] doubles = new double[split.length];
        for (int i = 0; i < split.length; i++) {
            String axis = split[i];
            axis = axis.trim().replace(",", "");
            doubles[i] = Double.parseDouble(axis);
        }

        return split.length >= 5
            ? new Vector(doubles[0], doubles[1], doubles[2], doubles[3], doubles[4])
            : new Vector(doubles[0], doubles[1], doubles[2]);
    }

    public static String toString(Vector vector) {
        return vector.yaw == 0D && vector.pitch == 0D
            ? vector.x + ", " + vector.y + ", " + vector.z
            : vector.x + ", " + vector.y + ", " + vector.z + ", " + vector.yaw + ", " + vector.pitch;
    }

    public Vector() {
    }

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(double x, double y, double z, double yaw, double pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vector add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector add(double x, double y, double z, double yaw, double pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
        return add(x, y, z);
    }

    public Vector remove(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector remove(double x, double y, double z, double yaw, double pitch) {
        this.yaw -= yaw;
        this.pitch -= pitch;
        return remove(x, y, z);
    }

    public Vector set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector set(double x, double y, double z, double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return set(x, y, z);
    }

    public Vector removeYawPitch() {
        this.yaw = 0;
        this.pitch = 0;
        return this;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    @Override
    public String toString() {
        return "Vector{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            ", yaw=" + yaw +
            ", pitch=" + pitch +
            '}';
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Vector clone() {
        return new Vector(x, y, z);
    }

    @Override
    public String deserialize() {
        return toString(this);
    }
}