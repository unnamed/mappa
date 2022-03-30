package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Vector;

public class CuboidPart implements CommandPart {
    private final String name;

    public CuboidPart(String name) {
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
        Vector pos1 = VectorPart.parse(stack);
        Vector pos2 = VectorPart.parse(stack);
        Cuboid cuboid = new Cuboid(pos1, pos2);
        context.setValue(this, cuboid);
    }
}
