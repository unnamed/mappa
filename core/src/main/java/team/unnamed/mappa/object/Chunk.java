package team.unnamed.mappa.object;

import java.util.Objects;

public class Chunk implements Deserializable {
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
            ints[i] = Integer.parseInt(axis);;
        }
        return new Chunk(ints[0], ints[1]);
    }

    public static String toString(Chunk chunk) {
        return chunk.getX() + ", " + chunk.getY();
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
