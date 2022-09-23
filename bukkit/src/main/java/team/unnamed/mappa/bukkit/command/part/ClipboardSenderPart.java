package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import net.kyori.text.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.object.Clipboard;
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
        CommandSender sender = context.getObject(CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);
        if (!(sender instanceof Player)) {
            throw new ArgumentParseException(TranslatableComponent.of("sender.only-player"));
        }
        Player player = (Player) sender;
        Clipboard clipboard = handler.getClipboardOf(player.getUniqueId());
        if (clipboard == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
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
