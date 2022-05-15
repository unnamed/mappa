package team.unnamed.mappa.model.region;

import team.unnamed.mappa.object.DeserializableList;
import team.unnamed.mappa.object.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Cuboid implements DeserializableList, Region<Vector> {
    protected final Vector maximum;
    protected final Vector minimum;

    public static Cuboid fromStrings(List<?> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("No enough string lines for cuboid");
        }

        Vector pos1 = Vector.fromString(String.valueOf(lines.get(0)));
        Vector pos2 = Vector.fromString(String.valueOf(lines.get(1)));
        return new Cuboid(pos1, pos2);
    }

    public static Cuboid fromStringsNoY(List<?> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("No enough string lines for cuboid");
        }

        Vector pos1 = Vector.fromStringNoY(String.valueOf(lines.get(0)));
        Vector pos2 = Vector.fromStringNoY(String.valueOf(lines.get(1)));
        return new Cuboid(pos1, pos2);
    }

    public Cuboid(Vector position1, Vector position2) {
        this.maximum = Vector.getMaximum(position1, position2);
        this.minimum = Vector.getMinimum(position1, position2);
    }

    public Vector getMaximum() {
        return maximum;
    }

    public Vector getMinimum() {
        return minimum;
    }

    public boolean contains(Vector object) {
        return Vector.isInAABB(object, maximum, minimum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuboid cuboid = (Cuboid) o;
        return Objects.equals(minimum, cuboid.minimum) && Objects.equals(maximum, cuboid.maximum);
    }

    @Override
    public String toString() {
        return "Cuboid{" +
            "maximum=" + maximum +
            ", minimum=" + minimum +
            '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(minimum, maximum);
    }

    @Override
    public List<String> deserialize() {
        return Arrays.asList(minimum.deserialize(), maximum.deserialize());
    }
}
