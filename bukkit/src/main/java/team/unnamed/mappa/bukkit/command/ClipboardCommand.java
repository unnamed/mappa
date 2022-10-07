package team.unnamed.mappa.bukkit.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.command.part.MapPropertyPathPart;
import team.unnamed.mappa.bukkit.command.part.Path;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.event.MappaPropertySetEvent;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.BlockFace;
import team.unnamed.mappa.util.Texts;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class ClipboardCommand extends HelpCommand {
    private final EventBus eventBus;
    private final ClipboardHandler handler;

    public ClipboardCommand(MappaCommand command) {
        super(command.getBootstrap().getTextHandler());
        this.eventBus = command.getBootstrap().getEventBus();
        this.handler = command.getPlugin().getClipboardHandler();
    }

    @Command(names = {"", "help", "?"})
    public void onHelp(CommandSender sender, CommandContext context) {
        help(sender, context);
    }

    @SuppressWarnings("unchecked")
    @Command(names = {"copy"})
    public void onCopy(@Sender Player player,
                       CommandContext context,
                       @Path(collect = true) String path) throws ArgumentTextParseException {
        Map<String, MapProperty> properties = context.getObject(
            Map.class, MapPropertyPathPart.PROPERTIES);
        Clipboard clipboard = MappaBukkit.newClipboard(handler, player, properties);
        if (clipboard.isEmpty()) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NOTHING_TO_COPY
                    .text());
        }
        textHandler.send(player,
            BukkitTranslationNode
                .CLIPBOARD_COPIED
                .text());
    }

    @Command(names = "paste")
    public void onPaste(@Sender Player player,
                        @Sender MapEditSession session,
                        @Sender Clipboard clipboard,
                        @Switch(value = "mirror", allowFullName = true) boolean mirrored) throws ParseException {
        Location location = MappaBukkit.getBlockLoc(player);
        clipboard.paste(BlockFace.yawToFace(location.getYaw()),
            MappaBukkit.toMappa(location),
            mirrored,
            session,
            (path, property) -> eventBus.callEvent(
                new MappaPropertySetEvent(player,
                    session,
                    path,
                    Texts.getActionSetTranslation(path, property, property.getValue()),
                    property,
                    true)));
        BukkitTranslationNode node = mirrored
            ? BukkitTranslationNode.CLIPBOARD_MIRROR_PASTED
            : BukkitTranslationNode.CLIPBOARD_PASTED;
        textHandler.send(player, node.text());
    }

    @Command(names = "cast-paste")
    public void onCastPaste(@Sender Player player,
                            @Sender MapEditSession session,
                            @Path String toCast,
                            @Sender Clipboard clipboard,
                            @Switch(value = "mirror", allowFullName = true) boolean mirrored) throws ParseException {
        Location location = MappaBukkit.getBlockLoc(player);
        clipboard.castPaste(BlockFace.yawToFace(location.getYaw()),
            MappaBukkit.toMappa(location),
            mirrored,
            session,
            toCast,
            (path, property) -> eventBus.callEvent(
                new MappaPropertySetEvent(player,
                    session,
                    path,
                    Texts.getActionSetTranslation(path, property, property.getValue()),
                    property,
                    true)));
        textHandler.send(player,
            BukkitTranslationNode
                .CLIPBOARD_CAST_PASTED
                .with("{new-path}", toCast));
    }
}
