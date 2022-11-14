package team.unnamed.mappa.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.command.parts.MapPropertyPathPart;
import team.unnamed.mappa.internal.command.parts.Path;
import team.unnamed.mappa.internal.command.parts.Sender;
import team.unnamed.mappa.internal.event.MappaPropertySetEvent;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.BlockFace;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class ClipboardCommand extends HelpCommand {
    private final EventBus eventBus;
    private final ClipboardHandler handler;

    public ClipboardCommand(MappaAPI api) {
        this.eventBus = api.getEventBus();
        this.handler = api.getClipboardHandler();
    }

    @Command(names = {"help", "?"})
    public void onHelp(MappaPlayer sender, @OptArg("1") int page, CommandContext context) {
        help(sender, page, context);
    }

    @SuppressWarnings("unchecked")
    @Command(names = {"copy"})
    public void onCopy(@Sender MappaPlayer player,
                       CommandContext context,
                       @Path(collect = true) String path) throws ArgumentTextParseException {
        Map<String, MapProperty> properties = context.getObject(
            Map.class, MapPropertyPathPart.PROPERTIES);
        Clipboard clipboard = handler.newCopyOfProperties(player, properties);
        if (clipboard.isEmpty()) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .NOTHING_TO_COPY
                    .text());
        }
        player.send(
            TranslationNode
                .CLIPBOARD_COPIED
                .text());
    }

    @Command(names = "paste")
    public void onPaste(@Sender MappaPlayer player,
                        @Sender MapSession session,
                        @Sender Clipboard clipboard,
                        @Switch(value = "mirror", allowFullName = true) boolean mirrored) throws ParseException {
        Vector position = player.getPosition();
        clipboard.paste(BlockFace.yawToFace(position.getYaw()),
            position,
            mirrored,
            session,
            (path, property) -> eventBus.callEvent(
                new MappaPropertySetEvent(player,
                    session,
                    path,
                    property,
                    true)));
        TranslationNode node = mirrored
            ? TranslationNode.CLIPBOARD_MIRROR_PASTED
            : TranslationNode.CLIPBOARD_PASTED;
        player.send(node.text());
    }

    @Command(names = "cast-paste")
    public void onCastPaste(@Sender MappaPlayer player,
                            @Sender MapSession session,
                            @Path String toCast,
                            @Sender Clipboard clipboard,
                            @Switch(value = "mirror", allowFullName = true) boolean mirrored) throws ParseException {
        Vector position = player.getPosition();
        clipboard.castPaste(BlockFace.yawToFace(position.getYaw()),
            position,
            mirrored,
            session,
            toCast,
            (path, property) -> eventBus.callEvent(
                new MappaPropertySetEvent(player,
                    session,
                    path,
                    property,
                    true)));
        player.send(
            TranslationNode
                .CLIPBOARD_CAST_PASTED
                .with("{new-path}", toCast));
    }
}
