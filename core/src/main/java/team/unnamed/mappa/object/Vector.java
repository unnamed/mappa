package team.unnamed.mappa.object;

public class Vector implements Cloneable, Deserializable {
    protected final double x;
    protected final double y;
    protected final double z;

    protected final double yaw;
    protected final double pitch;

    protected final boolean noY;

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

    public static Vector fromStringNoY(String line) {
        String[] split = line.split(",");
        if (split.length < 2) {
            throw new IllegalArgumentException("Insufficient arguments for vector");
        }
        double[] doubles = new double[split.length];
        for (int i = 0; i < split.length; i++) {
            String axis = split[i];
            axis = axis.trim().replace(",", "");
            doubles[i] = Double.parseDouble(axis);
        }

        double yaw = 0;
        double pitch = 0;
        if (split.length >= 4) {
            yaw = doubles[2];
            pitch = doubles[3];
        }
        return new Vector(doubles[0], 0, doubles[1], yaw, pitch, true);
    }

    public static String toString(Vector vector) {
        return vector.yaw == 0D && vector.pitch == 0D
            ? vector.x + ", " + vector.y + ", " + vector.z
            : vector.x + ", " + vector.y + ", " + vector.z + ", " + vector.yaw + ", " + vector.pitch;
    }

    public static String toStringNoY(Vector vector) {
        return vector.yaw == 0D && vector.pitch == 0D
            ? vector.x + ", " + vector.z
            : vector.x + ", " + vector.z + ", " + vector.yaw + ", " + vector.pitch;
    }

    public static boolean isInAABB(Vector point, Vector max, Vector min) {
        return point.getX() >= min.getX()
            && point.getY() >= min.getY()
            && point.getZ() >= min.getZ()
            && point.getX() <= max.getX()
            && point.getY() <= max.getY()
            && point.getZ() <= max.getZ();
    }

    public static Vector getMinimum(Vector v1, Vector v2) {
        return new Vector(
            Math.min(v1.getX(), v2.getX()),
            Math.min(v1.getY(), v2.getY()),
            Math.min(v1.getZ(), v2.getZ()),
            0,
            0,
            v1.isNoY() && v2.isNoY());
    }

    public static Vector getMaximum(Vector v1, Vector v2) {
        return new Vector(
            Math.max(v1.getX(), v2.getX()),
            Math.max(v1.getY(), v2.getY()),
            Math.max(v1.getZ(), v2.getZ()),
            0,
            0,
            v1.isNoY() && v2.isNoY());
    }

    public Vector(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public Vector(double x, double y, double z, double yaw, double pitch) {
        this(x, y, z, yaw, pitch, false);
    }

    public Vector(double x, double y, double z, double yaw, double pitch, boolean noY) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.noY = noY;
    }

    public Vector removeYawPitch() {
        return new Vector(x, y, z, 0, 0);
    }

    public Vector sum(double x, double y, double z) {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    public Vector mutX(double x) {
        return new Vector(x, this.y, this.z);
    }

    public Vector mutY(double y) {
        return new Vector(this.x, y, this.z);
    }

    public Vector mutZ(double z) {
        return new Vector(this.x, this.y, z);
    }

    public Vector sub(double x, double y, double z) {
        return new Vector(this.x - x, this.y - y, this.z - z);
    }

    public Vector mutYaw(double yaw) {
        return new Vector(x, y, z, yaw, pitch);
    }

    public Vector mutPitch(double pitch) {
        return new Vector(x, y, z, yaw, pitch);
    }

    public Vector mutYawPitch(double yaw, double pitch) {
        return new Vector(x, y, z, yaw, pitch);
    }

    public Vector mutNoY(boolean noY) {
        return new Vector(x, y, z, yaw, pitch, noY);
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

    public boolean isNoY() {
        return noY;
    }

    @Override
    public String toString() {
        return "Vector{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            ", yaw=" + yaw +
            ", pitch=" + pitch +
            ", noY=" + noY +
            '}';
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Vector clone() {
        return new Vector(x, y, z, 0, 0, noY);
    }

    @Override
    public String deserialize() {
        return noY ? toStringNoY(this) : toString(this);
    }
}