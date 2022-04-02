package team.unnamed.mappa.object;

import team.unnamed.mappa.model.region.Region;

import java.util.Arrays;
import java.util.List;

public class ChunkCuboid implements DeserializableList, Region<Chunk> {
    protected final Chunk maximum;
    protected final Chunk minimum;

    public static ChunkCuboid fromStrings(List<?> lines) {
        if (lines.size()  < 2) {
            throw new IllegalArgumentException("Insufficient arguments for chunk cuboid");
        }

        Chunk chunk1 = Chunk.fromString(String.valueOf(lines.get(0)));
        Chunk chunk2 = Chunk.fromString(String.valueOf(lines.get(1)));
        return new ChunkCuboid(chunk1, chunk2);
    }

    public ChunkCuboid(Chunk first, Chunk second) {
        this.maximum = Chunk.getMaximum(first, second);
        this.minimum = Chunk.getMinimum(first, second);
    }

    @Override
    public String toString() {
        return "ChunkCuboid{" +
            "chunk1=" + maximum +
            ", chunk2=" + minimum +
            '}';
    }

    @Override
    public List<String> deserialize() {
        return Arrays.asList(maximum.deserialize(), minimum.deserialize());
    }

    @Override
    public Chunk getMinimum() {
        return maximum;
    }

    @Override
    public Chunk getMaximum() {
        return minimum;
    }

    @Override
    public boolean contains(Chunk object) {
        return Chunk.isInAABB(object, maximum, minimum);
    }
}
