package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import team.unnamed.mappa.MappaPlatform;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

import java.util.Collections;
import java.util.List;

public class MapSessionSenderPart extends MapSessionPart {
    private final boolean sender;

    public MapSessionSenderPart(String name, boolean sender, MappaPlatform platform) {
        super(name, platform);
        this.sender = sender;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      CommandPart caller) throws ArgumentParseException {
        MappaPlayer sender = context.getObject(MappaPlayer.class,
            MappaCommandManager.MAPPA_PLAYER);
        if (!this.sender || sender.isConsole()) {
            super.parse(context, stack, caller);
            return;
        }

        MapSession session = sender.getMapSession();
        if (!(session instanceof MapEditSession)) {
            sender.send(BukkitTranslationNode
                .NO_SESSION_SELECTED
                .formalText());
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .SESSION_SELECT_GUIDE
                    .formalText());
        }
        context.setValue(this, session);
    }

    @Override
    public List<String> getSuggestions(CommandContext context, ArgumentStack stack) {
        return sender
            ? Collections.emptyList()
            : super.getSuggestions(context, stack);
    }
}
