package team.unnamed.mappa.object;

import team.unnamed.mappa.model.region.Region;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Cuboid implements DeserializableList, Region {
    protected final Vector maxVector;
    protected final Vector minVector;

    public static Cuboid fromStrings(List<?> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("No enough string lines for cuboid");
        }

        Vector pos1 = Vector.fromString(String.valueOf(lines.get(0)));
        Vector pos2 = Vector.fromString(String.valueOf(lines.get(1)));
        return new Cuboid(pos1, pos2);
    }

    public Cuboid(Vector position1, Vector position2) {
        this.maxVector = Vector.getMaximum(position1, position2);
        this.minVector = Vector.getMinimum(position1, position2);
    }

    public Vector getMaximum() {
        return maxVector;
    }

    public Vector getMinimum() {
        return minVector;
    }

    public boolean contains(Vector vector) {
        return Vector.isInAABB(vector, minVector, maxVector);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuboid cuboid = (Cuboid) o;
        return Objects.equals(minVector, cuboid.minVector) && Objects.equals(maxVector, cuboid.maxVector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minVector, maxVector);
    }

    @Override
    public List<String> deserialize() {
        return Arrays.asList(minVector.deserialize(), maxVector.deserialize());
    }
}
