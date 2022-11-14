package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.ErrorHandler;
import me.fixeddev.commandflow.annotated.part.Key;
import me.fixeddev.commandflow.command.Command;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public interface MappaCommandManager extends CommandManager {
    Key SESSION_KEY = new Key(MapSession.class);
    String MAPPA_PLAYER = "mappa player";

    Command registerMapScheme(MapScheme scheme) throws ParseException;

    void unregisterMapScheme(MapScheme scheme);

    Command getRootCommand(MapScheme scheme);

    Map<String, Command> getMapSchemeCommands();

    CommandManager getInternalCommandManager();

    CommandSchemeNodeBuilder getNodeBuilder();

    ErrorHandler getErrorHandler();

    void unregisterAll();
}
