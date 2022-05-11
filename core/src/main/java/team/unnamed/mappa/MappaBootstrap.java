package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.FileSource;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;

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
    private final Map<String, MapSession> sessionMap = new HashMap<>();
    @NotNull
    private final Set<String> toSave = new HashSet<>();
    @NotNull
    private final Map<String, MapSerializedSession> serializedSessionMap = new HashMap<>();
    @NotNull
    private final CommandSchemeNodeBuilder commandBuilder;
    @NotNull
    private final FileSource defaultSaveSource;
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
            return Collections.emptyList();
        }

        Map<String, Object> sessions = mapper.loadSessions(scheme, fileSource);
        List<MapSession> sessionList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : sessions.entrySet()) {
            Object object = entry.getValue();
            if (!(object instanceof Map)) {
                continue;
            }

            MapSession session = scheme.resumeSession(
                generateID(scheme), (Map<String, Object>) object);
            String id = session.getId();
            textHandler.send(sender, TranslationNode
                .LOAD_SESSION
                .withFormal("{id}", id));
            sessionList.add(session);
            sessionMap.put(id, session);
        }
        textHandler.send(sender, TranslationNode
            .SESSIONS_LOADED
            .withFormal("{number}", sessions.size()));
        return sessionList;
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
        if (sessionMap.containsKey(id)) {
            textHandler.send(sender, TranslationNode
                .SESSION_ALREADY_EXISTS
                .withFormal("{id}", id));
            return;
        }

        MapSession resumeSession = scheme.resumeSession(id, SchemeMapper.plainMap(session.getProperties()));
        sessionMap.put(id, resumeSession);
        serializedSessionMap.remove(id);
        textHandler.send(sender, TranslationNode
            .RESUME_SESSION
            .withFormal("{id}", id));
    }

    public List<MapSession> resumeSessions(boolean loadWarning) throws ParseException {
        return resumeSessions(null, loadWarning);
    }

    public List<MapSession> resumeSessions(Object entity, boolean loadWarning) throws ParseException {
        File sessionFile = new File(dataFolder, "sessions.yml");
        Set<String> blackListIds = new HashSet<>(sessionMap.keySet());
        Map<String, Object> serialized;
        try {
            if (sessionFile.exists()) {
                serialized = mapper.resumeSession(schemeRegistry,
                    loadWarning,
                    blackListIds,
                    sessionFile);
            } else {
                serialized = null;
            }
            if (serialized == null || serialized.isEmpty()) {
                textHandler.send(entity,
                    TranslationNode
                        .NO_SESSIONS_TO_RESUME
                        .formalText());
                return Collections.emptyList();
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

        List<MapSession> sessions = new ArrayList<>();
        List<MapSerializedSession> serializedSessions = new ArrayList<>();
        for (Object value : serialized.values()) {
            if (value instanceof MapSession) {
                MapSession session = (MapSession) value;
                if (session.isWarning()) {
                    textHandler.send(entity,
                        TranslationNode.SESSION_WARNING.formalText(),
                        session);
                }
                sessions.add(session);
            } else if (value instanceof MapSerializedSession) {
                MapSerializedSession serializedSession = (MapSerializedSession) value;
                MapSerializedSession.Reason reason = serializedSession.getReason();
                TranslationNode node = TranslationNode.valueOf("REASON_" + reason.name());
                textHandler.send(entity,
                    TranslationNode
                        .SESSION_IGNORED
                        .withFormal("{session_id}", serializedSession.getId(),
                            "{reason}", textHandler.format(entity, node.text())));
                serializedSessions.add(serializedSession);
            } else {
                throw new IllegalArgumentException(
                    "Unrecognized element in sessions.yml (Type: " + value.getClass().getSimpleName() + ")");
            }
        }

        sessions.forEach(session -> sessionMap.put(session.getId(), session));
        serializedSessions.forEach(session -> serializedSessionMap.put(session.getId(), session));
        textHandler.send(entity,
            TranslationNode
                .SESSIONS_RESUMED
                .withFormal(
                    "{number}", sessions.size()
                )
        );
        return sessions;
    }

    public MapSession newSession(MapScheme scheme) {
        return newSession(scheme, generateID(scheme));
    }

    public MapSession newSession(MapScheme scheme, String id) {
        MapSession session = sessionMap.get(id);
        if (session != null) {
            return null;
        }

        MapSession mySession = scheme.newSession(id);
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

    public void removeSession(Object sender, MapSerializedSession session) {
        String id = session.getId();
        serializedSessionMap.remove(id);
        textHandler.send(sender,
            TranslationNode
                .DELETE_SESSION
                .withFormal("{id}", id));
    }

    private String generateID(MapScheme scheme) {
        AtomicInteger counter = sessionCounter.computeIfAbsent(scheme,
            key -> new AtomicInteger());
        String id = scheme.getName() + "-" + counter.getAndIncrement();
        return sessionMap.containsKey(id) ? generateID(scheme) : id;
    }

    public void unload(Object sender, boolean saveIfReady) throws IOException {
        textHandler.send(sender, TranslationNode.UNLOAD_SCHEMES.formalText());
        schemeRegistry.clear();
        textHandler.send(sender, TranslationNode.UNLOAD_COMMANDS.formalText());
        commandManager.unregisterAll();
        saveAll(sender, saveIfReady);
    }

    public void saveAll(Object sender, boolean saveIfReady) throws IOException {
        if (sessionMap.isEmpty() && serializedSessionMap.isEmpty()) {
            return;
        }

        Map<MapScheme, File> writers = new HashMap<>();
        FileWriter serializeFile = new FileWriter(
            new File(dataFolder, "sessions.yml"));
        try {
            for (MapSession session : sessionMap.values()) {
                try {
                    MapScheme scheme = session.getScheme();
                    String id = session.getId();
                    Map<String, Text> errors = session.checkWithScheme(true);
                    if (!errors.isEmpty()) {
                        textHandler.send(sender,
                            TranslationNode
                                .CANNOT_SERIALIZE_SESSION
                                .formalText(),
                            session);
                        serialize(sender, serializeFile, session);
                        continue;
                    } else if (!saveIfReady && !toSave.contains(id)) {
                        serialize(sender, serializeFile, session);
                        continue;
                    }

                    File file = writers.computeIfAbsent(scheme,
                        key -> defaultSaveSource.file(
                            scheme,
                            dataFolder,
                            mapper.getFormatFile())
                    );

                    mapper.saveTo(file, session);
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

            for (MapSerializedSession session : serializedSessionMap.values()) {
                if (session.getReason() == MapSerializedSession.Reason.DUPLICATE) {
                    continue;
                }
                mapper.serializeTo(serializeFile, session);
            }
        } finally {
            for (File saveFile : writers.values()) {
                mapper.applySave(saveFile);
            }
        }
        textHandler.send(sender,
            TranslationNode.SAVED_FINISHED.formalText());
    }

    private void serialize(Object sender,
                           FileWriter serializeFile,
                           MapSession session)
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

    public MapScheme getScheme(String name) {
        return schemeRegistry.get(name);
    }

    public MapSession getSessionById(String id) {
        return sessionMap.get(id);
    }

    public MapSerializedSession getSerializedSessionById(String id) {
        return serializedSessionMap.get(id);
    }

    @NotNull
    public Map<String, MapSession> getSessionMap() {
        return sessionMap;
    }

    @NotNull
    public Map<String, MapSerializedSession> getSerializedSessionMap() {
        return serializedSessionMap;
    }

    public Collection<MapSession> getSessions() {
        return sessionMap.values();
    }

    public Collection<MapSerializedSession> getSerializedSessions() {
        return serializedSessionMap.values();
    }

    @NotNull
    public MappaTextHandler getTextHandler() {
        return textHandler;
    }

    @NotNull
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @NotNull
    public Map<String, MapScheme> getSchemeRegistry() {
        return schemeRegistry;
    }
}
