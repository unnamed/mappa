package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import net.kyori.text.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.model.MappaPlayer;

public class MappaPlayerPart implements CommandPart {
    private final String name;
    private final boolean onlyPlayer;

    public MappaPlayerPart(String name,
                           boolean onlyPlayer) {
        this.name = name;
        this.onlyPlayer = onlyPlayer;
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
        MappaPlayer player = context.getObject(MappaPlayer.class, MappaCommandManager.MAPPA_PLAYER);
        if (player.isConsole() && onlyPlayer) {
            throw new ArgumentParseException(TranslatableComponent.of("sender.only-player"));
        }
        context.setValue(this, player);
    }
}
