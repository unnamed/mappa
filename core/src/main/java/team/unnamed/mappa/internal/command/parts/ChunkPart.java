package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Chunk;

public class ChunkPart implements CommandPart {
    private final String name;

    public ChunkPart(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart part
    ) throws ArgumentParseException {
        Chunk chunk = parse(stack);
        context.setValue(this, chunk);
    }

    public static Chunk parse(ArgumentStack stack) throws ArgumentParseException {
        int x = stack.nextInt();
        int y = stack.nextInt();
        return new Chunk(x, y);
    }
}
