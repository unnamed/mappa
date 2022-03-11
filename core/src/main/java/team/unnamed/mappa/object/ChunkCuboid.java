package team.unnamed.mappa.object;

import java.util.Arrays;
import java.util.List;

public class ChunkCuboid implements DeserializableList {
    private final Chunk chunk1;
    private final Chunk chunk2;

    public static ChunkCuboid fromStrings(List<?> lines) {
        if (lines.size()  < 2) {
            throw new IllegalArgumentException("Insufficient arguments for chunk cuboid");
        }

        Chunk chunk1 = Chunk.fromString(String.valueOf(lines.get(0)));
        Chunk chunk2 = Chunk.fromString(String.valueOf(lines.get(1)));
        return new ChunkCuboid(chunk1, chunk2);
    }

    public ChunkCuboid(Chunk chunk1, Chunk chunk2) {
        this.chunk1 = chunk1;
        this.chunk2 = chunk2;
    }

    public Chunk getChunk1() {
        return chunk1;
    }

    public Chunk getChunk2() {
        return chunk2;
    }

    @Override
    public String toString() {
        return "ChunkCuboid{" +
            "chunk1=" + chunk1 +
            ", chunk2=" + chunk2 +
            '}';
    }

    @Override
    public List<String> deserialize() {
        return Arrays.asList(chunk1.deserialize(), chunk2.deserialize());
    }
}
