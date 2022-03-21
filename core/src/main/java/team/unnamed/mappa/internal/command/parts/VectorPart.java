package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;

public class VectorPart implements CommandPart {
    private final String name;

    public VectorPart(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void parse(CommandContext commandContext,
                      ArgumentStack argumentStack,
                      @Nullable CommandPart commandPart) throws ArgumentParseException {

    }
}
