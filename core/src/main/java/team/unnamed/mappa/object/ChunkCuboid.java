package team.unnamed.mappa.object;

import me.fixeddev.commandflow.stack.ArgumentStack;
import team.unnamed.mappa.internal.command.parts.ChunkPart;
import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.object.config.LineDeserializableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChunkCuboid implements LineDeserializableList, Region<Chunk> {
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

    public static ChunkCuboid parse(ArgumentStack stack) {
        Chunk chunk1 = ChunkPart.parse(stack);
        Chunk chunk2 = ChunkPart.parse(stack);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkCuboid that = (ChunkCuboid) o;
        return Objects.equals(maximum, that.maximum)
            && Objects.equals(minimum, that.minimum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maximum, minimum);
    }

    @Override
    public boolean contains(Chunk object) {
        return Chunk.isInAABB(object, maximum, minimum);
    }
}
