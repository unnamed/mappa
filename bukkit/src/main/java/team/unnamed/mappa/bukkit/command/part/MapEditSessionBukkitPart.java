package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.internal.command.parts.MapEditSessionPart;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

import java.util.Collections;
import java.util.List;

public class MapEditSessionBukkitPart extends MapEditSessionPart {
    private final boolean sender;

    public MapEditSessionBukkitPart(String name, boolean sender, MappaBootstrap bootstrap) {
        super(name, bootstrap);
        this.sender = sender;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      CommandPart caller) throws ArgumentParseException {
        CommandSender commandSender = context.getObject(CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);
        if (!sender || !(commandSender instanceof Player)) {
            super.parse(context, stack, caller);
            return;
        }

        Player player = (Player) commandSender;
        MapSession session = bootstrap.getSessionByEntity(player.getUniqueId());
        if (!(session instanceof MapEditSession)) {
            MappaTextHandler textHandler = bootstrap.getTextHandler();
            textHandler.send(player,
                BukkitTranslationNode
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
    public List<String> getSuggestions(CommandContext commandContext, ArgumentStack stack) {
        return sender
            ? Collections.emptyList()
            : super.getSuggestions(commandContext, stack);
    }
}
