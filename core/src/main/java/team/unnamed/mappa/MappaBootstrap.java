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
    private final MapSchemeFactory factory;
    @NotNull
    private final CommandManager commandManager;
    @NotNull
    private final MappaTextHandler textHandler;
    @NotNull
    private final ToolHandler toolHandler;

    @NotNull
    private final Map<String, MapScheme> schemeRegistry = new HashMap<>();
    @NotNull
    private final Map<MapScheme, AtomicInteger> sessionCounter = new HashMap<>();
    @NotNull
    private final RegionRegistry regionRegistry;
    @NotNull
    private final Map<String, List<MapSession>> mapSessionRegistry = new HashMap<>();
    @NotNull
    private final Map<String, MapSession> idSessionRegistry = new HashMap<>();
    @NotNull
    private final CommandSchemeNodeBuilder commandBuilder;
    @NotNull
    private final FileSource saveSource;

    @NotNull
    private final File dataFolder;
    private boolean loaded;

    public static Builder builder() {
        return new Builder();
    }

    public MappaBootstrap(@NotNull SchemeMapper mapper,
                          @NotNull File dataFolder,
                          @NotNull MapSchemeFactory factory,
                          @NotNull CommandManager commandManager,
                          @NotNull MappaTextHandler textHandler,
                          @NotNull ToolHandler toolHandler,
                          @NotNull RegionRegistry regionRegistry,
                          @NotNull PartInjector injector,
                          @NotNull EntityProvider provider,
                          @NotNull FileSource saveSource) {
        this.mapper = mapper;
        this.dataFolder = dataFolder;
        this.factory = factory;
        this.commandManager = commandManager;
        this.textHandler = textHandler;
        this.toolHandler = toolHandler;
        this.regionRegistry = regionRegistry;
        this.saveSource = saveSource;

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
            String mapName = entry.getKey();
            Object object = entry.getValue();
            if (!(object instanceof Map)) {
                continue;
            }

            MapSession session = scheme.resumeSession(mapName, (Map<String, Object>) object);
            sessionList.add(session);
            mapSessionRegistry.compute(session.getWorldName(),
                (name, list) -> addNewSession(list, session));
        }
        return sessionList;
    }

    private List<MapSession> addNewSession(List<MapSession> sessions, MapSession session) {
        if (sessions == null) {
            sessions = new ArrayList<>();
        }
        MapScheme scheme = session.getScheme();
        AtomicInteger counter = sessionCounter.computeIfAbsent(scheme,
            key -> new AtomicInteger());
        idSessionRegistry.put(
            scheme.getName() + "-" + counter.getAndIncrement(),
            session);
        sessions.add(session);
        return sessions;
    }

    public MapSession newSession(MapScheme scheme, String worldName) {
        MapSession session = scheme.newSession(worldName);
        List<MapSession> sessions = mapSessionRegistry.get(worldName);
        addNewSession(sessions, session);
        mapSessionRegistry.put(worldName, sessions);
        return session;
    }

    public void unload() throws IOException {
        schemeRegistry.clear();
        commandManager.unregisterAll();
        saveAll();
    }

    public void saveAll() throws IOException {
        Map<MapScheme, FileWriter> writers = new HashMap<>();
        try {
            for (List<MapSession> sessionList : mapSessionRegistry.values()) {
                for (MapSession session : sessionList) {
                    MapScheme scheme = session.getScheme();
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
        return idSessionRegistry.get(id);
    }

    @NotNull
    public List<MapSession> getSessions(String name) {
        return mapSessionRegistry.get(name);
    }

    @NotNull
    public Map<String, MapSession> getIdSessionRegistry() {
        return idSessionRegistry;
    }

    public <T> RegionSelection<T> getRegionSelectionOf(String id, Class<T> type) {
        return regionRegistry.getSelection(id, type);
    }

    public RegionSelection<Vector> getVectorSelectionOf(String id) {
        return getRegionSelectionOf(id, Vector.class);
    }

    public RegionSelection<Chunk> getChunkSelectionOf(String id) {
        return getRegionSelectionOf(id, Chunk.class);
    }

    @NotNull
    public MappaTextHandler getTextHandler() {
        return textHandler;
    }

    @NotNull
    public ToolHandler getToolHandler() {
        return toolHandler;
    }

    @NotNull
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @NotNull
    public Map<String, List<MapSession>> getMapSessionRegistry() {
        return mapSessionRegistry;
    }

    @NotNull
    public RegionRegistry getRegionRegistry() {
        return regionRegistry;
    }

    @NotNull
    public Map<String, MapScheme> getSchemeRegistry() {
        return schemeRegistry;
    }

    public static class Builder {
        private SchemeMapper mapper;
        private File dataFolder;
        private MapSchemeFactory factory;
        private CommandManager commandManager;
        private MappaTextHandler textHandler;
        private ToolHandler toolHandler;
        private RegionRegistry regionRegistry;
        private PartInjector injector;
        private EntityProvider provider;
        private FileSource saveSource;

        public Builder schemeMapper(SchemeMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder dataFolder(File dataFolder) {
            this.dataFolder = dataFolder;
            return this;
        }

        public Builder schemeFactory(MapSchemeFactory factory) {
            this.factory = factory;
            return this;
        }

        public Builder commandManager(CommandManager commandManager) {
            this.commandManager = commandManager;
            return this;
        }

        public Builder textHandler(MappaTextHandler textHandler) {
            this.textHandler = textHandler;
            return this;
        }

        public Builder toolHandler(ToolHandler toolHandler) {
            this.toolHandler = toolHandler;
            return this;
        }

        public Builder regionRegistry(RegionRegistry regionRegistry) {
            this.regionRegistry = regionRegistry;
            return this;
        }

        public Builder partInjector(PartInjector injector) {
            this.injector = injector;
            return this;
        }

        public Builder entityProvider(EntityProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder saveSource(FileSource saveSource) {
            this.saveSource = saveSource;
            return this;
        }

        public MappaBootstrap build() {
            if (saveSource == null) {
                saveSource = FileSource.BASIC;
            }
            return new MappaBootstrap(mapper,
                dataFolder,
                factory,
                commandManager,
                textHandler,
                toolHandler,
                regionRegistry,
                injector,
                provider,
                saveSource);
        }
    }
}
