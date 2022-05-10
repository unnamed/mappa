package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.function.EntityProvider;
import team.unnamed.mappa.internal.FileSource;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.object.Vector;
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
    private final FileSource saveSource;

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
                          @NotNull FileSource saveSource) {
        this.mapper = mapper;
        this.dataFolder = dataFolder;
        this.schemeFactory = schemeFactory;
        this.commandManager = commandManager;
        this.textHandler = textHandler;
        this.saveSource = saveSource;

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

    public List<MapSession> loadSessions(MapScheme scheme, Object entity) throws ParseException {
        File fileSource = saveSource.file(
            scheme, dataFolder, mapper.getFormatFile());
        if (!fileSource.exists()) {
            return Collections.emptyList();
        }

        Map<String, Object> sessions = mapper.loadSessions(scheme, fileSource);
        textHandler.send(entity, TranslationNode.SESSIONS_LOADED.withFormal());
        List<MapSession> sessionList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : sessions.entrySet()) {
            Object object = entry.getValue();
            if (!(object instanceof Map)) {
                continue;
            }

            MapSession session = scheme.resumeSession(
                generateID(scheme), (Map<String, Object>) object);
            sessionList.add(session);
            sessionMap.put(session.getId(), session);
        }
        return sessionList;
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

    public List<MapSession> resumeSessions(Object entity) throws ParseException {
        Set<String> blackListIds = new HashSet<>(sessionMap.keySet());
        File file = new File(dataFolder, "sessions.yml");
        Map<String, Object> serialized;
        try {
            if (file.exists()) {
                serialized = mapper.resumeSessions(schemeRegistry,
                    blackListIds,
                    file);
            } else {
                serialized = null;
            }
            if (serialized == null || serialized.isEmpty()) {
                textHandler.send(entity,
                    TranslationNode
                        .NO_SESSIONS_TO_RESUME
                        .formalText(
                        )
                );
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

    private String generateID(MapScheme scheme) {
        AtomicInteger counter = sessionCounter.computeIfAbsent(scheme,
            key -> new AtomicInteger());
        String id = scheme.getName() + "-" + counter.getAndIncrement();
        return sessionMap.containsKey(id) ? generateID(scheme) : id;
    }

    public void unload(Object sender) throws IOException {
        textHandler.send(sender, TranslationNode.UNLOAD_SCHEMES.formalText());
        schemeRegistry.clear();
        textHandler.send(sender, TranslationNode.UNLOAD_COMMANDS.formalText());
        commandManager.unregisterAll();
        saveAll(sender);
    }

    public void saveAll(Object sender) throws IOException {
        Map<MapScheme, FileWriter> writers = new HashMap<>();
        FileWriter serializeFile = new FileWriter(
            new File(dataFolder, "sessions.yml"));
        try {
            for (MapSession session : sessionMap.values()) {
                try {
                    MapScheme scheme = session.getScheme();
                    Map<String, Text> errMessage = session.checkWithScheme();
                    if (!errMessage.isEmpty()) {
                        mapper.serializeTo(serializeFile, session);
                        textHandler.send(sender,
                            TranslationNode
                                .SERIALIZE_SESSION
                                .formalText(),
                            session);
                        continue;
                    }

                    FileWriter writer = writers.computeIfAbsent(scheme,
                        key -> {
                            try {
                                return saveSource.fileWriter(
                                    scheme,
                                    dataFolder,
                                    mapper.getFormatFile());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    );

                    mapper.saveTo(writer, session);
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
            for (FileWriter writer : writers.values()) {
                writer.close();
            }
        }
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
