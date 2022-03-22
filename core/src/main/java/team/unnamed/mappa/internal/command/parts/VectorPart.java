package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Vector;

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
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart part) throws ArgumentParseException {
        Vector vector = parse(stack);
        context.setValue(this, vector);
    }

    public static Vector parse(ArgumentStack stack) {
        double x = stack.nextDouble();
        double y = stack.nextDouble();
        double z = stack.nextDouble();
        return new Vector(x, y, z);
    }
}
