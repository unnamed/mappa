package team.unnamed.mappa;

import me.fixeddev.commandflow.command.Command;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.FileSource;
import team.unnamed.mappa.internal.MapRegistry;
import team.unnamed.mappa.internal.MapRegistryImpl;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.event.MappaNewSessionEvent;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.player.PlayerRegistry;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.throwable.ParseRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("unchecked")
public class MappaPlatformImpl implements MappaPlatform {
    private final MappaAPI api;

    @NotNull
    private final SchemeMapper mapper;
    @NotNull
    private final MapSchemeFactory schemeFactory;
    @NotNull
    private final MappaCommandManager commandManager;
    @NotNull
    private final MappaTextHandler textHandler;

    @NotNull
    private final MapRegistry mapRegistry = new MapRegistryImpl();
    private final PlayerRegistry<?> playerRegistry;
    @NotNull
    private final Set<String> toSave = new HashSet<>();
    @NotNull
    private final FileSource defaultSaveSource;
    @NotNull
    private final EventBus eventBus;
    @NotNull
    private final Map<MapScheme, FileSource> saveSource = new HashMap<>();

    @NotNull
    private final File dataFolder;
    private boolean loaded;

    public MappaPlatformImpl(MappaAPI api,
                             @NotNull SchemeMapper mapper,
                             @NotNull MapSchemeFactory schemeFactory,
                             @NotNull File dataFolder,
                             @NotNull MappaCommandManager commandManager) {
        this.api = api;
        this.mapper = mapper;
        this.playerRegistry = api.getPlayerRegistry();
        this.dataFolder = dataFolder;
        this.schemeFactory = schemeFactory;
        this.commandManager = commandManager;
        this.textHandler = api.getTextHandler();
        this.defaultSaveSource = FileSource.SCHEME;
        this.eventBus = api.getEventBus();
    }

    @Override
    public void loadMapScheme(MappaPlayer sender, File schemeFile) throws ParseException {
        if (loaded) {
            return;
        }

        Map<String, Object> load = mapper.load(schemeFile);
        sender.send(
            TranslationNode
                .SCHEME_LOADED
                .withFormal(
                    "{number}", load.size()
                ));
        for (Map.Entry<String, Object> entry : load.entrySet()) {
            String schemeName = entry.getKey();
            Map<String, Object> map = (Map<String, Object>) entry.getValue();
            MapScheme scheme = schemeFactory.from(schemeName, map);
            mapRegistry.registerMapScheme(scheme);

            Command rootCommand = commandManager.registerMapScheme(scheme);
            List<String> aliases = rootCommand.getAliases();
            sender.send(
                TranslationNode.SCHEME_COMMAND_LOADED.withFormal(
                    "{name}", rootCommand.getName(),
                    "{aliases}", aliases,
                    "{scheme_name}", scheme.getName()
                )
            );
        }

        sender.send(
            TranslationNode.LOAD_SUCCESSFULLY.formalText());
        this.loaded = true;
    }

    @Override
    public List<MapSession> loadSessions(MapScheme scheme) throws ParseException {
        return loadSessions(playerRegistry.console(), scheme);
    }

    @Override
    public List<MapSession> loadSessions(MappaPlayer sender, MapScheme scheme) throws ParseException {
        FileSource source = saveSource.get(scheme);
        if (source == null) {
            source = defaultSaveSource;
        }

        File fileSource = source.file(
            scheme, dataFolder, mapper.getFormatFile());
        if (!fileSource.exists()) {
            sender.send(
                TranslationNode
                    .NO_SESSIONS_TO_LOAD
                    .formalText());
            return Collections.emptyList();
        }

        Map<String, Object> sessions = mapper.loadSessions(scheme, fileSource);
        List<MapSession> sessionList = new ArrayList<>();
        if (sessions == null || sessions.isEmpty()) {
            sender.send(
                TranslationNode
                    .NO_SESSIONS_TO_LOAD
                    .formalText());
            return sessionList;
        }

        try {
            for (Map.Entry<String, Object> entry : sessions.entrySet()) {
                Object object = entry.getValue();
                if (!(object instanceof Map)) {
                    continue;
                }


                Map<String, Object> properties = (Map<String, Object>) object;
                String id = generateID(scheme);
                resumeSession(sender, id, scheme, properties);
            }
        } catch (ParseException e) {
            sender.send(e.getTextNode());
            throw e;
        } catch (ParseRuntimeException e) {
            sender.send(
                e.getTextNode());
            throw e;
        }
        sender.send(TranslationNode
            .SESSIONS_LOADED
            .withFormal("{number}", sessions.size()));
        return sessionList;
    }

    @Override
    public MapEditSession resumeSession(MappaPlayer sender,
                                        String id,
                                        MapScheme scheme,
                                        Map<String, Object> properties) throws ParseException {
        String path = scheme.getObject(MapScheme.SESSION_ID_PATH);
        MapEditSession session = scheme.resumeSession(id, properties);
        if (path != null) {
            MapProperty property = session.getProperty(path);
            id = (String) property.getValue();
            session.setId(id);
        }

        if (mapRegistry.containsMapSessionId(id)) {
            sender.send(
                TranslationNode
                    .LOAD_SESSION_WITH_ID_EXISTS
                    .withFormal("{id}", id));
            String oldId = id;
            id = generateStringID(id);
            sender.send(
                TranslationNode
                    .LOAD_SESSION_ID_CHANGED
                    .withFormal("{id}", oldId,
                        "{new-id}", id));
            session.setId(id);
            if (path != null) {
                session.property(path, id);
            }
        }

        sender.send(
            TranslationNode
                .LOAD_SESSION
                .withFormal("{id}", id));
        mapRegistry.registerMapSession(session);
        eventBus.callEvent(new MappaNewSessionEvent(
            sender, session, MappaNewSessionEvent.Reason.RESUMED));
        return session;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadFileSources(MappaPlayer sender, Map<String, String> schemeToPath) throws IOException {
        for (Map.Entry<String, String> entry : schemeToPath.entrySet()) {
            String schemeName = entry.getKey();
            String path = entry.getValue();

            MapScheme scheme = getScheme(schemeName);
            if (scheme == null) {
                continue;
            }

            File file = new File(dataFolder, path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            saveSource.put(scheme, FileSource.asSource(file));
            sender.send(
                TranslationNode
                    .LOAD_FILE_SOURCE
                    .withFormal("{path}", path,
                        "{id}", schemeName));
        }
    }

    @Override
    public MapEditSession newSession(MappaPlayer sender, MapScheme scheme) throws ParseException {
        return newSession(sender, scheme, generateID(scheme));
    }

    @Override
    public MapEditSession newSession(MappaPlayer sender, MapScheme scheme, String id) throws ParseException {
        if (mapRegistry.containsMapSessionId(id)) {
            return null;
        }

        MapEditSession mySession = scheme.newSession(id);
        mapRegistry.registerMapSession(mySession);
        eventBus.callEvent(new MappaNewSessionEvent(
            sender, mySession, MappaNewSessionEvent.Reason.CREATED));
        return mySession;
    }

    public void removeSession(MappaPlayer sender, MapSession session) {
        mapRegistry.unregisterMapSession(session.getId());
        sender.send(
            TranslationNode
                .DELETE_SESSION
                .withFormal("{id}", session.getId()));
    }

    private String generateID(MapScheme scheme) {
        String id = scheme.getName() + "-" + scheme.getAndIncrementCounter();
        return mapRegistry.containsMapSessionId(id) ? generateID(scheme) : id;
    }

    @Override
    public String generateStringID(String prefix) {
        // Loop thought existing ids to generate
        // a non-collide id
        int i = 2;
        String id = prefix + "-" + i;
        while (mapRegistry.containsMapSessionId(id)) {
            id = prefix + "-" + i;
            ++i;
        }
        return id;
    }

    @Override
    public void unload(MappaPlayer sender) throws IOException {
        sender.send(TranslationNode.UNLOAD_SCHEMES.formalText());
        mapRegistry.unregisterAll();
        sender.send(TranslationNode.UNLOAD_COMMANDS.formalText());
        commandManager.unregisterAll();
        saveAll(sender);
    }

    @Override
    public void saveAll(MappaPlayer sender) throws IOException {
        Map<MapScheme, File> writers = new HashMap<>();
        try {
            for (MapSession session : mapRegistry.getMapSessions()) {
                try {
                    if (!(session instanceof MapEditSession)) {
                        throw new UnsupportedOperationException(
                            "Mappa bootstrap cannot handle "
                                + session.getClass().getSimpleName()
                                + " Map session type.");
                    }
                    MapEditSession editSession = (MapEditSession) session;
                    MapScheme scheme = session.getScheme();
                    File file = writers.computeIfAbsent(scheme,
                        key -> {
                            FileSource source = saveSource.get(scheme);
                            if (source == null) {
                                source = defaultSaveSource;
                            }
                            File sourceFile = source.file(
                                scheme,
                                dataFolder,
                                mapper.getFormatFile());
                            if (!sourceFile.exists()) {
                                try {
                                    sourceFile.createNewFile();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            return sourceFile;
                        }
                    );

                    mapper.saveTo(file, editSession);
                    sender.send(
                        TranslationNode
                            .SAVED_SESSION
                            .formalText(),
                        session);
                } catch (Exception e) {
                    e.printStackTrace();
                    // FIXME: Add a method to save session here
                }
            }
        } finally {
            for (File saveFile : writers.values()) {
                mapper.applySave(saveFile);
            }
        }

        sender.send(
            TranslationNode.SAVED_FINISHED.formalText());
    }

    public void markToSave(MappaPlayer sender, String id) {
        this.toSave.add(id);

        sender.send(
            TranslationNode
                .SESSION_MARK_SAVE
                .withFormal("{id}", id));
    }

    @Override
    public MapRegistry getMapRegistry() {
        return mapRegistry;
    }

    @Override
    public MapScheme getScheme(String name) {
        return mapRegistry.getMapScheme(name);
    }

    @Override
    public MapSession getMapSessionById(String id) {
        return mapRegistry.getMapSession(id);
    }

    @NotNull
    @Override
    public MappaTextHandler getTextHandler() {
        return textHandler;
    }


    @NotNull
    @Override
    public MappaCommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public CommandSchemeNodeBuilder getCommandBuilder() {
        return commandManager.getNodeBuilder();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public MappaAPI getApi() {
        return api;
    }
}
