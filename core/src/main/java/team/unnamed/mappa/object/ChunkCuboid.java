package team.unnamed.mappa.object;

public class ChunkCuboid {
    private final Chunk chunk1;
    private final Chunk chunk2;

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
}
