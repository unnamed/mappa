package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.FileSource;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.event.EventBus;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.throwable.ParseRuntimeException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class MappaBootstrap {
    @NotNull
    private final SchemeMapper mapper;
    @NotNull
    private final MapSchemeFactory schemeFactory;
    @NotNull
    private final CommandManager commandManager;
    @NotNull
    private final MappaTextHandler textHandler;

    @NotNull
    private final Map<String, MapScheme> schemeRegistry = new HashMap<>();
    @NotNull
    private final Map<MapScheme, AtomicInteger> sessionCounter = new HashMap<>();
    @NotNull
    private final Map<String, MapSession> sessionMap = new LinkedHashMap<>();
    @NotNull
    private final Set<String> toSave = new HashSet<>();
    @NotNull
    private final CommandSchemeNodeBuilder commandBuilder;
    @NotNull
    private final FileSource defaultSaveSource;
    @NotNull
    private final EventBus eventBus = new EventBus();
    @NotNull
    private final Map<MapScheme, FileSource> saveSource = new HashMap<>();

    @NotNull
    private final File dataFolder;
    private boolean loaded;

    public MappaBootstrap(@NotNull SchemeMapper mapper,
                          @NotNull MapSchemeFactory schemeFactory,
                          @NotNull File dataFolder,
                          @NotNull CommandManager commandManager,
                          @NotNull PartInjector injector,
                          @NotNull MappaTextHandler textHandler) {
        this(mapper,
            schemeFactory,
            dataFolder,
            commandManager,
            injector,
            textHandler,
            FileSource.SCHEME);
    }

    public MappaBootstrap(@NotNull SchemeMapper mapper,
                          @NotNull MapSchemeFactory schemeFactory,
                          @NotNull File dataFolder,
                          @NotNull CommandManager commandManager,
                          @NotNull PartInjector injector,
                          @NotNull MappaTextHandler textHandler,
                          @NotNull FileSource defaultSaveSource) {
        this.mapper = mapper;
        this.dataFolder = dataFolder;
        this.schemeFactory = schemeFactory;
        this.commandManager = commandManager;
        this.textHandler = textHandler;
        this.defaultSaveSource = defaultSaveSource;

        this.commandBuilder = CommandSchemeNodeBuilder.builder(injector, textHandler);
    }

    public void loadSchemes(File schemeFile) throws ParseException {
        loadSchemes(schemeFile, null);
    }

    public void loadSchemes(File schemeFile, Object sender) throws ParseException {
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
            MapScheme scheme = schemeFactory.from(schemeName, map);
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

    public List<MapSession> loadSessions(MapScheme scheme) throws ParseException {
        return loadSessions(scheme, null);
    }

    public List<MapSession> loadSessions(MapScheme scheme, Object sender) throws ParseException {
        FileSource source = saveSource.get(scheme);
        if (source == null) {
            source = defaultSaveSource;
        }

        File fileSource = source.file(
            scheme, dataFolder, mapper.getFormatFile());
        if (!fileSource.exists()) {
            textHandler.send(sender,
                TranslationNode
                    .NO_SESSIONS_TO_LOAD
                    .formalText());
            return Collections.emptyList();
        }

        Map<String, Object> sessions = mapper.loadSessions(scheme, fileSource);
        List<MapSession> sessionList = new ArrayList<>();
        if (sessions == null || sessions.isEmpty()) {
            textHandler.send(sender,
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
            textHandler.send(sender,
                e.getTextNode());
            throw e;
        } catch (ParseRuntimeException e) {
            textHandler.send(sender,
                e.getTextNode());
            throw e;
        }
        textHandler.send(sender, TranslationNode
            .SESSIONS_LOADED
            .withFormal("{number}", sessions.size()));
        return sessionList;
    }

    public MapEditSession resumeSession(Object sender,
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

        if (sessionMap.containsKey(id)) {
            textHandler.send(sender,
                TranslationNode
                    .LOAD_SESSION_WITH_ID_EXISTS
                    .withFormal("{id}", id));
            String oldId = id;
            id = generateStringID(id);
            textHandler.send(sender,
                TranslationNode
                    .LOAD_SESSION_ID_CHANGED
                    .withFormal("{id}", oldId,
                        "{new-id}", id));
            session.setId(id);
            if (path != null) {
                session.property(path, id);
            }
        }

        textHandler.send(sender,
            TranslationNode
                .LOAD_SESSION
                .withFormal("{id}", id));
        sessionMap.put(id, session);
        return session;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadFileSources(Object sender, Map<String, String> schemeToPath) throws IOException {
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
            textHandler.send(sender,
                TranslationNode
                    .LOAD_FILE_SOURCE
                    .withFormal("{path}", path,
                        "{id}", schemeName));
        }
    }

    public void resumeSession(Object sender, MapSerializedSession session) throws ParseException {
        String schemeName = session.getSchemeName();
        MapScheme scheme = getScheme(schemeName);
        if (scheme == null) {
            textHandler.send(sender, TranslationNode
                .SCHEME_NOT_FOUND
                .withFormal("{id}", schemeName));
            return;
        }

        String id = session.getId();
        Map<String, Object> properties = SchemeMapper.plainMap(
            session.getSerializedProperties());
        MapEditSession resumeSession = resumeSession(sender, id, scheme, properties);
        id = resumeSession.getId();
        textHandler.send(sender, TranslationNode
            .RESUME_SESSION
            .withFormal("{id}", id));
    }

    public void resumeSessions(boolean loadWarning) throws ParseException {
        resumeSessions(null, loadWarning);
    }

    public void resumeSessions(Object entity, boolean loadWarning) throws ParseException {
        File sessionFile = new File(dataFolder, "sessions.yml");
        Map<String, Object> serialized;
        try {
            if (sessionFile.exists()) {
                serialized = mapper.resumeSessions(entity,
                    this,
                    loadWarning,
                    sessionFile);
            } else {
                serialized = null;
            }

            if (serialized == null || serialized.isEmpty()) {
                textHandler.send(entity,
                    TranslationNode
                        .NO_SESSIONS_TO_RESUME
                        .formalText());
                return;
            }
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ParseException) {
                ParseException exception = (ParseException) cause;
                throw new RuntimeException(
                    textHandler.format(entity, exception.getTextNode()));
            }
            throw e;
        }

        int sessions = 0;
        for (Object value : serialized.values()) {
            if (!(value instanceof MapSession)) {
                String name = value == null
                    ? "null"
                    : value.getClass().getSimpleName();
                throw new IllegalArgumentException(
                    "Unrecognized element in sessions.yml "
                        + "(Type: " + name + ")");
            }

            if (value instanceof MapEditSession) {
                MapEditSession editSession = (MapEditSession) value;
                if (editSession.isWarning()) {
                    textHandler.send(entity,
                        TranslationNode
                            .SESSION_WARNING
                            .formalText(),
                        editSession);
                }
            } else if (value instanceof MapSerializedSession) {
                MapSerializedSession serializedSession = (MapSerializedSession) value;
                textHandler.send(entity,
                    TranslationNode
                        .SESSION_SERIALIZED
                        .formalText(),
                    serializedSession);
            }

            ++sessions;
        }

        textHandler.send(entity,
            TranslationNode
                .SESSIONS_RESUMED
                .withFormal("{number}", sessions));
    }

    public MapEditSession newSession(MapScheme scheme) {
        return newSession(scheme, generateID(scheme));
    }

    public MapEditSession newSession(MapScheme scheme, String id) {
        MapSession session = sessionMap.get(id);
        if (session != null) {
            return null;
        }

        MapEditSession mySession = scheme.newSession(id);
        sessionMap.put(mySession.getId(), mySession);
        return mySession;
    }

    public void removeSession(Object sender, MapSession session) {
        String id = session.getId();
        sessionMap.remove(id);
        textHandler.send(sender,
            TranslationNode
                .DELETE_SESSION
                .withFormal("{id}", id));
    }

    private String generateID(MapScheme scheme) {
        AtomicInteger counter = sessionCounter.computeIfAbsent(scheme,
            key -> new AtomicInteger(1));
        String id = scheme.getName() + "-" + counter.getAndIncrement();
        return sessionMap.containsKey(id) ? generateID(scheme) : id;
    }

    public String generateStringID(String prefix) {
        // Loop thought existing ids to generate
        // a non-collide id
        int i = 2;
        String id = prefix + "-" + i;
        while (sessionMap.containsKey(id)) {
            id = prefix + "-" + i;
            ++i;
        }
        return id;
    }

    public void unload(Object sender, boolean saveIfReady) throws IOException {
        textHandler.send(sender, TranslationNode.UNLOAD_SCHEMES.formalText());
        schemeRegistry.clear();
        textHandler.send(sender, TranslationNode.UNLOAD_COMMANDS.formalText());
        commandManager.unregisterAll();
        saveAll(sender, saveIfReady, true);
    }

    public void saveAll(Object sender, boolean saveIfReady, boolean clearRegistry) throws IOException {
        if (sessionMap.isEmpty()) {
            return;
        }

        Map<MapScheme, File> writers = new HashMap<>();
        FileWriter serializeFile = new FileWriter(
            new File(dataFolder, "sessions.yml"));
        try {
            for (MapSession session : sessionMap.values()) {
                try {
                    if (session instanceof MapSerializedSession) {
                        MapSerializedSession serialized = (MapSerializedSession) session;
                        if (serialized.getReason() == MapSerializedSession.Reason.DUPLICATE) {
                            continue;
                        }

                        mapper.serializeTo(serializeFile, serialized);
                        return;
                    } else if (!(session instanceof MapEditSession)) {
                        throw new UnsupportedOperationException(
                            "Mappa bootstrap cannot handle "
                                + session.getClass().getSimpleName()
                                + " Map session type.");
                    }
                    MapEditSession editSession = (MapEditSession) session;
                    MapScheme scheme = session.getScheme();
                    String id = session.getId();
                    Map<String, Text> errors = editSession.checkWithScheme(true);
                    if (!errors.isEmpty()) {
                        textHandler.send(sender,
                            TranslationNode
                                .CANNOT_SERIALIZE_SESSION
                                .formalText(),
                            session);
                        serialize(sender, serializeFile, editSession);
                        continue;
                    } else if (!saveIfReady && !toSave.contains(id)) {
                        serialize(sender, serializeFile, editSession);
                        continue;
                    }

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
                    textHandler.send(sender,
                        TranslationNode
                            .SAVED_SESSION
                            .formalText(),
                        session);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Trying to serialize the session to save the progress
                    session.setWarning(true);
                    mapper.serializeTo(serializeFile, session);
                }
            }
        } finally {
            for (File saveFile : writers.values()) {
                mapper.applySave(saveFile);
            }
        }

        if (clearRegistry) {
            sessionMap.clear();
            textHandler.send(sender,
                TranslationNode
                    .UNLOAD_MAP_SESSIONS
                    .formalText());
        }

        textHandler.send(sender,
            TranslationNode.SAVED_FINISHED.formalText());
    }

    private void serialize(Object sender,
                           FileWriter serializeFile,
                           MapEditSession session)
        throws IOException {
        mapper.serializeTo(serializeFile, session);
        textHandler.send(sender,
            TranslationNode
                .SERIALIZE_SESSION
                .formalText(),
            session);
    }

    public void markToSave(Object sender, String id) {
        this.toSave.add(id);

        textHandler.send(sender,
            TranslationNode
                .SESSION_MARK_SAVE
                .withFormal("{id}", id));
    }

    public boolean resolve(MapSerializedSession session) {
        MapSerializedSession.Reason reason = session.getReason();
        switch (reason) {
            case BLACK_LIST:
                return false;
            case DUPLICATE:
                String id = session.getId();
                return sessionMap.containsKey(id);
            case WARNING:
            default:
                return !session.isWarning();
        }
    }

    public MapScheme getScheme(String name) {
        return schemeRegistry.get(name);
    }

    public MapSession getSessionById(String id) {
        return sessionMap.get(id);
    }

    public boolean containsSessionID(String id) {
        return sessionMap.containsKey(id);
    }

    public MapSerializedSession newMapSerializedSession(String id,
                                                        MapScheme scheme,
                                                        MapSerializedSession.Reason reason,
                                                        boolean warning,
                                                        Map<String, Object> properties) {
        MapSerializedSession session = new MapSerializedSession(id,
            scheme,
            reason,
            true,
            properties);
        sessionMap.put(id, session);
        return session;
    }

    @NotNull
    public Map<String, MapSession> getSessionMap() {
        return sessionMap;
    }

    public Collection<MapSession> getSessions() {
        return sessionMap.values();
    }

    @NotNull
    public MappaTextHandler getTextHandler() {
        return textHandler;
    }

    @NotNull
    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CommandSchemeNodeBuilder getCommandBuilder() {
        return commandBuilder;
    }

    @NotNull
    public Map<String, MapScheme> getSchemeRegistry() {
        return schemeRegistry;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
