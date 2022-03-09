package team.unnamed.mappa.object;

import java.util.List;
import java.util.Objects;

public class Cuboid {
    protected Vector position1;
    protected Vector position2;

    protected Vector maxVector;
    protected Vector minVector;

    public static Cuboid fromStrings(List<?> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("No enough string lines for cuboid");
        }

        Vector pos1 = Vector.fromString(String.valueOf(lines.get(0)));
        Vector pos2 = Vector.fromString(String.valueOf(lines.get(1)));
        return new Cuboid(pos1, pos2);
    }

    public Cuboid(Vector position1, Vector position2) {
        this.position1 = position1;
        this.position2 = position2;
    }

    public Vector getPosition1() {
        return position1;
    }

    public Vector getPosition2() {
        return position2;
    }

    public void setPosition1(Vector position1) {
        this.position1 = position1;

        this.maxVector = null;
        this.minVector = null;
    }

    public void setPosition2(Vector position2) {
        this.position2 = position2;

        this.maxVector = null;
        this.minVector = null;
    }

    public Vector getMaxVector() {
        calcVectors();
        return maxVector;
    }

    public Vector getMinVector() {
        calcVectors();
        return minVector;
    }

    public boolean contains(Vector vector) {
        calcVectors();
        return Vector.isInAABB(vector, minVector, maxVector);
    }

    public void calcVectors() {
        if (maxVector == null || minVector == null) {
            this.maxVector = Vector.getMaximum(position1, position2);
            this.minVector = Vector.getMinimum(position1, position2);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuboid cuboid = (Cuboid) o;
        return Objects.equals(position1, cuboid.position1) && Objects.equals(position2, cuboid.position2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position1, position2);
    }

    @Override
    public String toString() {
        return "Cuboid{" +
            "position1=" + position1 +
            ", position2=" + position2 +
            '}';
    }
}
