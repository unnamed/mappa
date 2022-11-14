package team.unnamed.mappa.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.ErrorHandler;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaPlatform;
import team.unnamed.mappa.internal.MapRegistry;
import team.unnamed.mappa.internal.command.parts.Sender;
import team.unnamed.mappa.internal.event.MappaSavedEvent;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.ParseException;

@Command(names = {"session"},
    permission = "mappa.session.control")
public class MapSessionCommand extends HelpCommand {
    private final MappaPlatform platform;

    public MapSessionCommand(MappaAPI api) {
        this.platform = api.getPlatform();
    }

    @Command(names = {"help", "?"})
    public void onHelp(MappaPlayer sender, @OptArg("1") int page, CommandContext context) {
        help(sender, page, context);
    }

    @Command(names = {"create"},
        permission = "mappa.session.control")
    public void newSession(MappaPlayer sender,
                           MapScheme scheme,
                           @OptArg("") String id) throws ParseException {
        MapSession session = id.isEmpty()
            ? platform.newSession(sender, scheme)
            : platform.newSession(sender, scheme, id);
        if (session == null) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SESSION_ALREADY_EXISTS
                    .withFormal("{id}", id));
        }
        sender.send(
            TranslationNode
                .NEW_SESSION
                .formalText(),
            session
        );

        if (sender.isConsole()) {
            return;
        }

        select(sender, session);
    }

    @Command(names = "select")
    public void select(MappaPlayer player, MapSession session) {
        player.selectMapSession(session);
    }

    @Command(names = "deselect")
    public void deselect(MappaPlayer player) {
        player.deselectMapSession();
    }

    @Command(names = {"info"},
        permission = "mappa.session.setup")
    public void showSessionInfo(MappaPlayer sender,
                                @Sender MapSession session) {
        sender.showSessionInfo(session);
    }

    @Command(names = "show-setup",
        permission = "mappa.session.setup")
    public void showSetup(MappaPlayer sender) throws ParseException {
        sender.showSetup();
    }

    @Command(names = "verify",
        permission = "mappa.session.setup")
    public void verify(MappaPlayer sender,
                       @Switch("all") boolean showAll) throws ParseException {
        sender.verifyMapSession(showAll);
    }

    @Command(names = {"list", "ls", "sessions"},
        permission = "mappa.session.list")
    public void showSessions(MappaPlayer sender) {
        sender.showSessionList();
    }

    @Command(names = "load",
        permission = "mappa.session.control")
    public void loadSessions(MappaPlayer sender,
                             MapScheme scheme) throws ParseException {
        platform.loadSessions(sender, scheme);
    }

    @Command(names = {"save-all"},
        permission = "mappa.session.control")
    public void saveAllSession(CommandContext context,
                               MappaPlayer sender) throws Throwable {
        try {
            platform.saveAll(sender);
        } catch (Exception e) {
            ErrorHandler errorHandler = platform.getCommandManager().getErrorHandler();
            errorHandler.handleException(context, e);
        }
    }

    @Command(names = {"save"},
        permission = "mappa.session.control")
    public void saveSession(MappaPlayer sender, MapSession session) {
        platform.markToSave(sender, session.getId());
        platform.getEventBus()
            .callEvent(new MappaSavedEvent(sender, session));
    }

    @Command(names = {"delete"},
        permission = "mappa.session.control")
    public void deleteSession(MappaPlayer sender, MapSession session) {
        platform.removeSession(sender, session);
    }

    @Command(names = "set-id",
        permission = "mappa.session.control")
    public void setId(MappaPlayer sender,
                      MapSession session,
                      String newId) throws ParseException {
        MapRegistry registry = platform.getMapRegistry();
        if (registry.containsMapSessionId(newId)) {
            sender.send(
                BukkitTranslationNode
                    .SESSION_ALREADY_EXISTS
                    .withFormal("{id}", newId));
            return;
        }

        String oldId = session.getId();
        session.setId(newId);
        registry.unregisterMapSession(oldId);
        registry.registerMapSession(session);
        sender.send(
            BukkitTranslationNode
                .SESSION_ID_SET
                .withFormal("{old}", oldId,
                    "{new}", newId));
        MapScheme scheme = session.getScheme();
        String path = scheme.getObject(MapScheme.SESSION_ID_PATH);
        if (path == null) {
            return;
        }

        session.property(path, newId);
        sender.send(
            TranslationNode
                .PROPERTY_CHANGE_TO
                .withFormal("{name}", path,
                    "{value}", newId));
    }

    @Command(names = "set-warning",
        permission = "mappa.session.control")
    public void switchWarning(MappaPlayer sender,
                              MapSession session,
                              boolean warning) {
        session.setWarning(warning);
        sender.send(
            BukkitTranslationNode.SESSION_WARNING_SET.formalText(),
            session
        );
    }
}
