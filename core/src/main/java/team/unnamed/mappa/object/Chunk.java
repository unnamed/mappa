package team.unnamed.mappa.object;

import java.util.Objects;

public class Chunk {
    protected final int x;
    protected final int y;

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
}
