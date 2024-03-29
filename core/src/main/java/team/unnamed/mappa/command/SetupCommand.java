package team.unnamed.mappa.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.ContextSnapshotAccessor;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaPlatform;
import team.unnamed.mappa.internal.command.parts.Sender;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.throwable.ParseException;

public class SetupCommand implements CommandClass {
    private final CommandManager commandManager;
    private final MappaPlatform platform;

    public SetupCommand(MappaAPI api) {
        this.platform = api.getPlatform();
        this.commandManager = platform.getCommandManager();
    }

    @Command(names = {"skip-setup", "skip"},
        permission = "mappa.session.setup")
    public void skipSetupProperty(CommandContext context,
                                  @Sender MappaPlayer sender,
                                  @Sender MapSession session) throws ParseException {
        String setupStep = session.currentSetup();
        MapProperty property = session.getProperty(setupStep);
        if (!property.isOptional()) {
            sender.send(BukkitTranslationNode.NO_OPTIONAL_SETUP.formalText());
            return;
        }

        session.skipSetup();
        sender.send(" ");
        setupProperty(context, sender, session, null);
    }

    @Command(names = "setup",
        permission = "mappa.session.setup")
    public void setupProperty(CommandContext context,
                              @Sender MappaPlayer sender,
                              @Sender MapSession session,
                              @OptArg("") String arg) throws ParseException {
        if (!session.setup()) {
            sender.send(BukkitTranslationNode.NO_SETUP.formalText());
            return;
        }

        String setupStep = session.currentSetup();
        String line = session.getSchemeName()
                      + " "
                      + setupStep.replace(".", " ");
        if (arg == null) {
            sender.showSetup();
            return;
        }

        if (!arg.isEmpty()) {
            line += " " + arg;
        }

        try {
            commandManager.execute(ContextSnapshotAccessor.getNamespaceOf(context), line);
        } catch (Exception e) {
            e.printStackTrace();
            sender.send(" ");
            sender.showSetup();
            return;
        }
    }

    @Command(names = "show-setup",
        permission = "mappa.session.setup")
    public void showSetup(@Sender MappaPlayer sender,
                          @Sender MapSession session) throws ParseException {
        sender.showSetup();
    }
}
