package team.unnamed.mappa.object;

import me.fixeddev.commandflow.stack.ArgumentStack;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.config.LineDeserializable;

import java.util.Objects;

public class Vector implements Cloneable, LineDeserializable {
    protected final double x;
    protected final double y;
    protected final double z;

    protected final double yaw;
    protected final double pitch;

    protected boolean yawPitch;
    protected boolean block;
    protected final boolean noY;

    public static Vector fromString(String line) {
        return fromString(line, false, true);
    }

    public static Vector fromString(String line, boolean yawPitch, boolean block) {
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

        Vector vector = split.length >= 5
            ? new Vector(doubles[0], doubles[1], doubles[2], doubles[3], doubles[4])
            : new Vector(doubles[0], doubles[1], doubles[2]);
        vector.setYawPitch(yawPitch);
        vector.setBlock(block);
        return vector;
    }

    public static Vector fromStringNoY(String line) {
        return fromStringNoY(line, false, false);
    }

    public static Vector fromStringNoY(String line, boolean yawPitch, boolean block) {
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
        return new Vector(doubles[0], 0, doubles[1], yaw, pitch, yawPitch, true, block);
    }

    public static String toString(Vector vector) {
        double x = vector.x;
        double y = vector.y;
        double z = vector.z;
        if (vector.isBlock()) {
            x = Math.floor(x);
            y = Math.floor(y);
            z = Math.floor(z);
        }
        return !vector.isYawPitch() || vector.yaw == 0D && vector.pitch == 0D
            ? x + ", " + y + ", " + z
            : x + ", " + y + ", " + z + ", " + vector.yaw + ", " + vector.pitch;
    }

    public static String toStringNoY(Vector vector) {
        double x = vector.x;
        double z = vector.z;
        if (vector.isBlock()) {
            x = Math.floor(x);
            z = Math.floor(z);
        }
        return !vector.isYawPitch() || vector.yaw == 0D && vector.pitch == 0D
            ? x + ", " + z
            : x + ", " + z + ", " + vector.yaw + ", " + vector.pitch;
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
            v1.isYawPitch() && v2.isYawPitch(),
            v1.isNoY() && v2.isNoY(),
            v1.isBlock() && v2.isBlock());
    }

    public static Vector getMaximum(Vector v1, Vector v2) {
        return new Vector(
            Math.max(v1.getX(), v2.getX()),
            Math.max(v1.getY(), v2.getY()),
            Math.max(v1.getZ(), v2.getZ()),
            0,
            0,
            v1.isYawPitch() && v2.isYawPitch(),
            v1.isNoY() && v2.isNoY(),
            v1.isBlock() && v2.isBlock());
    }

    public static Vector distance(Vector from, Vector to) {
        double x = to.getX() - from.getX();
        double y = to.getY() - from.getY();
        double z = to.getZ() - from.getZ();
        return new Vector(x, y, z);
    }

    public static Vector blockDistance(Vector from, Vector to) {
        double x = to.getX() - (int) from.getX();
        double y = to.getY() - (int) from.getY();
        double z = to.getZ() - (int) from.getZ();
        return new Vector(x, y, z);
    }
    
    public static Vector parse(ArgumentStack stack) {
        double x = stack.nextDouble();
        double y = stack.nextDouble();
        double z = stack.nextDouble();
        return new Vector(x, y, z);
    }

    public Vector(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public Vector(double x, double y, double z, double yaw, double pitch) {
        this(x, y, z, yaw, pitch, false, false, false);
    }

    public Vector(double x, double y, double z, double yaw, double pitch, boolean yawPitch, boolean noY, boolean block) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.yawPitch = yawPitch;
        this.noY = noY;
        this.block = block;
    }

    public Vector distance(Vector to) {
        return distance(this, to);
    }

    public Vector removeYawPitch() {
        return new Vector(x, y, z, 0, 0);
    }

    public Vector setYawPitch(boolean yawPitch) {
        this.yawPitch = yawPitch;
        return this;
    }

    public Vector setBlock(boolean block) {
        this.block = block;
        return this;
    }

    public Vector sum(double x, double y, double z) {
        return sum(x, y, z, 0D, 0D);
    }

    public Vector sum(double x, double y, double z, double yaw, double pitch) {
        return new Vector(this.x + x,
            this.y + y,
            this.z + z,
            this.yaw + yaw,
            this.pitch + pitch,
            yawPitch,
            noY,
            block);
    }


    public Vector sum(Vector vector) {
        return sum(vector.getX(),
            vector.getY(),
            vector.getZ());
    }

    public Vector sumBlock(Vector vector) {
        return sum(
            Math.round(vector.getX()),
            Math.round(vector.getY()),
            Math.round(vector.getZ()));
    }

    public Vector mutX(double x) {
        return new Vector(x,
            this.y,
            this.z,
            this.yaw,
            this.pitch,
            yawPitch,
            noY,
            block);
    }

    public Vector mutY(double y) {
        return new Vector(this.x,
            y,
            this.z,
            this.yaw,
            this.pitch,
            yawPitch,
            noY,
            block);
    }

    public Vector mutZ(double z) {
        return new Vector(this.x,
            this.y,
            z,
            this.yaw,
            this.pitch,
            yawPitch,
            noY,
            block);
    }

    public Vector sub(double x, double y, double z) {
        return sub(x, y, z, 0D, 0D);
    }

    public Vector sub(double x, double y, double z, double yaw, double pitch) {
        return new Vector(this.x - x,
            this.y - y,
            this.z - z,
            this.yaw - yaw,
            this.pitch - pitch,
            yawPitch,
            noY,
            block);
    }

    public Vector sub(Vector vector) {
        return sub(vector.getX(),
            vector.getY(),
            vector.getZ(),
            vector.getYaw(),
            vector.getPitch());
    }

    public Cuboid expand(int x, int y, int z) {
        Vector min = sub(x, y, z);
        Vector max = sum(x, y, z);
        return new Cuboid(min, max);
    }

    public Cuboid expand(int x, int yPlus, int yMinus, int z) {
        Vector min = sub(x, yMinus, z);
        Vector max = sum(x, yPlus, z);
        return new Cuboid(min, max);
    }

    public Vector mutYaw(double yaw) {
        return new Vector(x, y, z, yaw, pitch, yawPitch, noY, block);
    }

    public Vector mutPitch(double pitch) {
        return new Vector(x, y, z, yaw, pitch, yawPitch, noY, block);
    }

    public Vector mutYawPitch(double yaw, double pitch) {
        return new Vector(x, y, z, yaw, pitch, yawPitch, noY, block);
    }

    public Vector mutNoY(boolean noY) {
        return new Vector(x, y, z, yaw, pitch, yawPitch, noY, block);
    }

    public Vector mutBlock(boolean block) {
        return new Vector(x, y, z, yaw, pitch, yawPitch, noY, block);
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

    public boolean isYawPitch() {
        return yawPitch;
    }

    public boolean isBlock() {
        return block;
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
            ", block=" + block +
            '}';
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Vector clone() {
        return new Vector(x, y, z, yaw, pitch, yawPitch, noY, block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector vector = (Vector) o;
        return Double.compare(vector.x, x) == 0
            && Double.compare(vector.y, y) == 0
            && Double.compare(vector.z, z) == 0
            && Double.compare(vector.yaw, yaw) == 0
            && Double.compare(vector.pitch, pitch) == 0;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch);
    }

    @Override
    public String deserialize() {
        return noY ? toStringNoY(this) : toString(this);
    }
}