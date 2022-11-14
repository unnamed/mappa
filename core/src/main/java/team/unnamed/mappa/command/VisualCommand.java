package team.unnamed.mappa.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import team.unnamed.mappa.internal.command.parts.Path;
import team.unnamed.mappa.internal.command.parts.Sender;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;

@Command(names = {"visual"}, permission = "mappa.visual")
public class VisualCommand extends HelpCommand {

    @Command(names = {"help", "?"})
    public void onHelp(MappaPlayer sender, @OptArg("1") int page, CommandContext context) {
        help(sender, page, context);
    }

    @Command(
        names = "show-visual",
        permission = "mappa.session.setup")
    public void showVisual(@Sender MappaPlayer player,
                           @Sender MapSession session,
                           @Path String path) {
        player.showVisual(path);
    }

    @Command(
        names = "hide-visual",
        permission = "mappa.session.setup")
    public void hideVisual(@Sender MappaPlayer player,
                           @Sender MapSession session,
                           @Path String path) {
        player.hideVisual(path);
    }
}
