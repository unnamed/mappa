package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

public class MapSerializedSessionPart implements CommandPart {
    private final String name;
    private final MappaBootstrap bootstrap;

    public MapSerializedSessionPart(String name, MappaBootstrap bootstrap) {
        this.name = name;
        this.bootstrap = bootstrap;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart caller)
        throws ArgumentParseException {
        String id = stack.next();
        MapSerializedSession serialized = bootstrap.getSerializedSessionById(id);
        if (serialized == null) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SERIALIZED_SESSION_NOT_FOUND
                    .withFormal("{id}", id)
            );
        }

        context.setValue(this, serialized);
    }
}
