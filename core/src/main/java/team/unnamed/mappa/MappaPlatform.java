package team.unnamed.mappa;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.MapRegistry;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Platform base to run Mappa and components
 * agnostic of the environment itself.
 */
public interface MappaPlatform {

    static MappaPlatformBuilder builder(MappaAPI api) {
        return new MappaPlatformImplBuilder(api);
    }

    void saveAll(MappaPlayer sender) throws IOException;

    MapRegistry getMapRegistry();

    MapScheme getScheme(String name);

    MapSession getMapSessionById(String id);

    @NotNull MappaTextHandler getTextHandler();

    @NotNull MappaCommandManager getCommandManager();

    CommandSchemeNodeBuilder getCommandBuilder();

    EventBus getEventBus();

    MappaAPI getApi();

    void loadMapScheme(MappaPlayer console, File file) throws ParseException;

    List<MapSession> loadSessions(MapScheme scheme) throws ParseException;

    List<MapSession> loadSessions(MappaPlayer sender, MapScheme scheme) throws ParseException;

    MapSession resumeSession(MappaPlayer sender,
                                 String id,
                                 MapScheme scheme,
                                 Map<String, Object> properties) throws ParseException;

    void loadFileSources(MappaPlayer console, Map<String, String> mapSources) throws IOException;

    MapSession newSession(MappaPlayer sender, MapScheme scheme) throws ParseException;

    MapSession newSession(MappaPlayer sender, MapScheme scheme, String id) throws ParseException;

    String generateStringID(String prefix);

    void unload(MappaPlayer console) throws IOException;

    void markToSave(MappaPlayer sender, String id);

    void removeSession(MappaPlayer sender, MapSession session);
}
