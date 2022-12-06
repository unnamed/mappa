package team.unnamed.mappa.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import team.unnamed.mappa.internal.command.parts.MapPropertyPathPart;
import team.unnamed.mappa.internal.command.parts.Path;
import team.unnamed.mappa.internal.command.parts.PropertyType;
import team.unnamed.mappa.internal.command.parts.Sender;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class ClipboardCommand extends HelpCommand {

    @Command(names = {"help", "?"})
    public void onHelp(MappaPlayer sender, @OptArg("1") int page, CommandContext context) {
        help(sender, page, context);
    }

    @SuppressWarnings("unchecked")
    @Command(names = {"copy"})
    public void onCopy(@Sender MappaPlayer player,
                       CommandContext context,
                       @Path(find = PropertyType.ALL, collect = true) String path)
        throws ArgumentTextParseException {
        Map<String, MapProperty> properties = context.getObject(
            Map.class, MapPropertyPathPart.PROPERTIES);
        player.copy(properties);
    }

    @Command(names = {"clear"})
    public void onClear(@Sender MappaPlayer player) throws ArgumentTextParseException {
        player.clearClipboard();
    }

    @Command(names = "paste")
    public void onPaste(@Sender MappaPlayer player,
                        @Sender MapSession session,
                        @Switch(value = "mirror", allowFullName = true) boolean mirrored) throws ParseException {
        player.paste(mirrored);
    }

    @Command(names = "cast-paste")
    public void onCastPaste(@Sender MappaPlayer player,
                            @Sender MapSession session,
                            @Path(find = PropertyType.SECTION) String toCast,
                            @Switch(value = "mirror", allowFullName = true) boolean mirrored) throws ParseException {
        player.castPaste(toCast, mirrored);
    }
}
