package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import net.kyori.text.TranslatableComponent;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

public class ClipboardSenderPart implements CommandPart {
    private final String name;
    private final ClipboardHandler handler;

    public ClipboardSenderPart(String name, ClipboardHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      CommandPart part)
        throws ArgumentParseException {
        MappaPlayer sender = context.getObject(MappaPlayer.class,
            MappaCommandManager.MAPPA_PLAYER);
        if (sender.isConsole()) {
            throw new ArgumentParseException(TranslatableComponent.of("sender.only-player"));
        }
        Clipboard clipboard = handler.getClipboardOf(sender);
        if (clipboard == null) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .NO_CLIPBOARD
                    .text());
        }

        context.setValue(this, clipboard);
    }

    @Override
    public String getName() {
        return name;
    }
}
