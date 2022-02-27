package team.unnamed.mappa.object;

import java.util.Objects;

public class Cuboid {
    protected Vector position1;
    protected Vector position2;

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
    }

    public void setPosition2(Vector position2) {
        this.position2 = position2;
    }

    public boolean contains(Vector vector) {
        return false;
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
