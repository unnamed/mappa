package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import team.unnamed.mappa.function.EntityProvider;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.configuration.InterpretMode;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappaBootstrap {
    private final SchemeMapper mapper;
    private final MapSchemeFactory factory;
    private final CommandManager commandManager;
    private final MappaTextHandler textHandler;

    private final Map<String, MapScheme> schemeRegistry = new HashMap<>();
    private final Map<String, List<MapSession>> sessionRegistry = new HashMap<>();
    private final CommandSchemeNodeBuilder commandBuilder;

    private boolean loaded;

    public MappaBootstrap(SchemeMapper mapper,
                          MapSchemeFactory factory,
                          CommandManager commandManager,
                          MappaTextHandler textHandler,
                          PartInjector injector,
                          EntityProvider provider
    ) {
        this.mapper = mapper;
        this.factory = factory;
        this.commandManager = commandManager;
        this.textHandler = textHandler;

        this.commandBuilder = CommandSchemeNodeBuilder.builder(injector, textHandler, provider);
    }

    public void load(File schemeFile) throws ParseException {
        load(schemeFile, null);
    }

    public void load(File schemeFile, Object sender) throws ParseException {
        if (loaded) {
            return;
        }

        Map<String, Object> load = mapper.load(schemeFile);
        textHandler.send(sender,
            TranslationNode
                .SCHEME_LOADED
                .withFormal(
                    "{number}", load.size()
                ));
        for (Map.Entry<String, Object> entry : load.entrySet()) {
            String schemeName = entry.getKey();
            Map<String, Object> map = (Map<String, Object>) entry.getValue();
            MapScheme scheme = factory.from(schemeName, map);
            schemeRegistry.put(schemeName, scheme);

            Command rootCommand = commandBuilder.fromScheme(scheme);
            List<String> aliases = rootCommand.getAliases();
            textHandler.send(sender,
                TranslationNode.SCHEME_COMMAND_LOADED.withFormal(
                    "{name}", rootCommand.getName(),
                    "{aliases}", aliases,
                    "{scheme_name}", scheme.getName()
                )
            );
            commandManager.registerCommand(rootCommand);
        }

        textHandler.send(sender,
            TranslationNode.LOAD_SUCCESSFULLY.formalText());
        this.loaded = true;
    }

    public List<MapSession> loadSessions(MapScheme scheme, File file) throws ParseException {
        return loadSessions(scheme, file, null);
    }

    public List<MapSession> loadSessions(MapScheme scheme, File file, Object entity) throws ParseException {
        Map<String, Object> sessions = mapper.loadSessions(scheme, file);
        textHandler.send(entity, TranslationNode.SESSIONS_LOADED.withFormal());
        List<MapSession> sessionList = new ArrayList<>();
        if (scheme.getInterpretMode() == InterpretMode.NODE_PER_MAP) {
            for (Map.Entry<String, Object> entry : sessions.entrySet()) {
                String mapName = entry.getKey();
                Object object = entry.getValue();
                if (!(object instanceof Map)) {
                    continue;
                }

                MapSession session = scheme.resumeSession(mapName, (Map<String, Object>) object);
                sessionList.add(session);
                sessionRegistry.compute(session.getWorldName(),
                    (name, list) -> addNewSession(list, session));
            }
        } else {
            MapSession session = scheme.resumeSession(file.getName(), sessions);
            sessionList.add(session);
            sessionRegistry.compute(session.getWorldName(),
                (name, list) -> addNewSession(list, session));
        }
        return sessionList;
    }

    private List<MapSession> addNewSession(List<MapSession> sessions, MapSession session) {
        if (sessions == null) {
            sessions = new ArrayList<>();
        }
        sessions.add(session);
        return sessions;
    }

    public MapSession newSession(MapScheme scheme, String worldName) {
        MapSession session = scheme.newSession(worldName);
        List<MapSession> sessions = sessionRegistry.get(worldName);
        addNewSession(sessions, session);
        sessionRegistry.put(worldName, sessions);
        return session;
    }

    public void unload() {
        schemeRegistry.clear();
        commandManager.unregisterAll();
    }

    public MapScheme getScheme(String name) {
        return schemeRegistry.get(name);
    }

    public List<MapSession> getSessions(String name) {
        return sessionRegistry.get(name);
    }

    public MappaTextHandler getTextHandler() {
        return textHandler;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Map<String, List<MapSession>> getSessionRegistry() {
        return sessionRegistry;
    }

    public Map<String, MapScheme> getSchemeRegistry() {
        return schemeRegistry;
    }
}
