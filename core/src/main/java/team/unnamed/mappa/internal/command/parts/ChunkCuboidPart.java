package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;

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
    public void parse(CommandContext commandContext,
                      ArgumentStack argumentStack,
                      @Nullable CommandPart commandPart)
        throws ArgumentParseException {

    }
}
