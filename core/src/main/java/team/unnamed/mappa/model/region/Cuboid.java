package team.unnamed.mappa.model.region;

import me.fixeddev.commandflow.stack.ArgumentStack;
import team.unnamed.mappa.internal.command.parts.VectorPart;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.object.config.LineDeserializableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Cuboid implements LineDeserializableList, Region<Vector> {
    public static final double ITERATION_SEPARATOR = 1;

    protected final Vector maximum;
    protected final Vector minimum;

    public static Cuboid fromStrings(List<?> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("No enough string lines for cuboid");
        }

        // No yaw pitch for cuboid position
        Vector pos1 = Vector.fromString(String.valueOf(lines.get(0)));
        Vector pos2 = Vector.fromString(String.valueOf(lines.get(1)));
        return new Cuboid(pos1, pos2);
    }

    public static Cuboid fromStringsNoY(List<?> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("No enough string lines for cuboid");
        }

        // No yaw pitch for cuboid position
        Vector pos1 = Vector.fromStringNoY(String.valueOf(lines.get(0)));
        Vector pos2 = Vector.fromStringNoY(String.valueOf(lines.get(1)));
        return new Cuboid(pos1, pos2);
    }

    public static Cuboid parse(ArgumentStack stack) {
        Vector pos1 = VectorPart.parse(stack);
        Vector pos2 = VectorPart.parse(stack);
        return new Cuboid(pos1, pos2);
    }

    public Cuboid(Vector position1, Vector position2) {
        this.maximum = Vector.getMaximum(position1, position2);
        this.minimum = Vector.getMinimum(position1, position2);

        // Never operate without block vectors!
        this.maximum.setBlock(true);
        this.minimum.setBlock(true);
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

    public void forEach(Position consumer) {
        for (double x = minimum.getX(); x <= maximum.getX(); x++) {
            for (double y = minimum.getY(); y <= maximum.getY(); y++) {
                for (double z = minimum.getZ(); z <= maximum.getZ(); z++) {
                    consumer.accept(x, y, z);
                }
            }
        }
    }

    public void forEachCorner(Position consumer) {
        forEachCorner(consumer,
            ITERATION_SEPARATOR,
            ITERATION_SEPARATOR,
            ITERATION_SEPARATOR);
    }

    public void forEachCorner(Position consumer,
                              double xSeparator,
                              double ySeparator,
                              double zSeparator) {
        double minimumX = minimum.getX();
        double minimumY = minimum.getY();
        double minimumZ = minimum.getZ();

        // +1 to get the max corner
        double maximumX = maximum.getX() + 1;
        double maximumY = maximum.getY() + 1;
        double maximumZ = maximum.getZ() + 1;
        for (double x = minimumX; x <= maximumX; x += xSeparator) {
            for (double y = minimumY; y <= maximumY; y += ySeparator) {
                for (double z = minimumZ; z <= maximumZ; z += zSeparator) {
                    if (x == maximumX || x == minimumX ||
                        y == maximumY || y == minimumY ||
                        z == maximumZ || z == minimumZ) {
                        consumer.accept(x, y, z);
                    }
                }
            }
        }
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
