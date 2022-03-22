package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;

public class ChunkCuboidPart implements CommandPart {
    private final String name;

    public ChunkCuboidPart(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart part)
        throws ArgumentParseException {
        Chunk chunk1 = ChunkPart.parse(stack);
        Chunk chunk2 = ChunkPart.parse(stack);
        ChunkCuboid chunkCuboid = new ChunkCuboid(chunk1, chunk2);
        context.setValue(this, chunkCuboid);
    }
}
