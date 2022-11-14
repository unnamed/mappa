package team.unnamed.mappa.object;

import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.stack.ArgumentStack;
import team.unnamed.mappa.object.config.LineDeserializable;

import java.util.Objects;

public class Chunk implements LineDeserializable {
    protected final int x;
    protected final int y;

    public static Chunk fromString(String line) {
        String[] split = line.split(",");
        if (split.length < 2) {
            throw new IllegalArgumentException("Insufficient arguments for chunk");
        }

        int[] ints = new int[2];
        for (int i = 0; i < 2; i++) {
            String axis = split[i];
            axis = axis.trim().replace(",", "");
            ints[i] = Integer.parseInt(axis);
            ;
        }
        return new Chunk(ints[0], ints[1]);
    }

    public static String toString(Chunk chunk) {
        return chunk.getX() + ", " + chunk.getY();
    }

    public static boolean isInAABB(Chunk point, Chunk max, Chunk min) {
        return point.getX() >= min.getX()
            && point.getY() >= min.getY()
            && point.getX() <= max.getX()
            && point.getY() <= max.getY();
    }

    public static Chunk getMaximum(Chunk first, Chunk second) {
        return new Chunk(
            Math.max(first.getX(), second.getX()),
            Math.max(first.getY(), second.getY())
        );
    }

    public static Chunk getMinimum(Chunk first, Chunk second) {
        return new Chunk(
            Math.min(first.getX(), second.getX()),
            Math.min(first.getY(), second.getY())
        );
    }

    public static Chunk parse(ArgumentStack stack) throws ArgumentParseException {
        int x = stack.nextInt();
        int y = stack.nextInt();
        return new Chunk(x, y);
    }

    public Chunk(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Chunk{" +
            "x=" + x +
            ", y=" + y +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return x == chunk.x && y == chunk.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String deserialize() {
        return toString(this);
    }
}
