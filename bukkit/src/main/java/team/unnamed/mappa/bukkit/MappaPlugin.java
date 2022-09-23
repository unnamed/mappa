package team.unnamed.mappa.bukkit;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.ErrorHandler;
import me.fixeddev.commandflow.annotated.AnnotatedCommandTreeBuilder;
import me.fixeddev.commandflow.annotated.part.Key;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import me.fixeddev.commandflow.bukkit.factory.BukkitModule;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.exception.CommandUsage;
import me.fixeddev.commandflow.translator.Translator;
import me.yushust.message.bukkit.BukkitMessageAdapt;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.adapter.bukkit.TextAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.command.MappaCommand;
import team.unnamed.mappa.bukkit.command.part.MappaBukkitPartModule;
import team.unnamed.mappa.bukkit.internal.BukkitVisualizer;
import team.unnamed.mappa.bukkit.internal.CacheRegionRegistry;
import team.unnamed.mappa.bukkit.internal.GettableTranslationProvider;
import team.unnamed.mappa.bukkit.listener.SelectionListener;
import team.unnamed.mappa.bukkit.render.CuboidRender;
import team.unnamed.mappa.bukkit.render.VectorRender;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.text.YamlFile;
import team.unnamed.mappa.bukkit.tool.*;
import team.unnamed.mappa.function.EntityProvider;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.clipboard.ClipboardHandlerImpl;
import team.unnamed.mappa.internal.clipboard.CuboidTransform;
import team.unnamed.mappa.internal.clipboard.VectorTransform;
import team.unnamed.mappa.internal.color.ColorScheme;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.event.*;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.injector.BasicMappaModule;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.internal.message.MappaColorTranslator;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.message.MessageTranslationProvider;
import team.unnamed.mappa.internal.message.placeholder.MapSessionPlaceholder;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.Texts;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MappaPlugin extends JavaPlugin implements MappaAPI {
    public static final EntityProvider BUKKIT_SENDER =
        context -> context.getObject(
            CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);

    private MappaCommand command;

    private MappaBootstrap bootstrap;
    private EventBus eventBus;
    private MappaTextHandler textHandler;
    private ToolHandler toolHandler;
    private RegionRegistry regionRegistry;
    private BukkitVisualizer visualizer;
    private ClipboardHandler clipboardHandler;

    private final Map<Integer, Consumer<Projectile>> projectileCache = new HashMap<>();

    private FileConfiguration mainConfig;
    private VisualizerTask task;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        this.mainConfig = getConfig();
        File schemes = new File(getDataFolder(), "schemes.yml");
        if (!schemes.exists()) {
            saveResource("schemes.yml", false);
        }
        File colorConfig = new File(getDataFolder(), "colors.yml");
        if (!colorConfig.exists()) {
            saveResource("colors.yml", false);
        }

        List<TextDefault> list = asTranslation(TranslationNode.values(), BukkitTranslationNode.values());
        GettableTranslationProvider provider = new GettableTranslationProvider();
        list.addAll(provider.toTexts("commandflow."));

        YamlFile.refillFileWith(
            this,
            "lang_en",
            list
        );
    }

    @SafeVarargs
    public final <T extends TextDefault> List<TextDefault> asTranslation(T[]... defaults) {
        List<TextDefault> defaultList = new ArrayList<>();
        for (T[] array : defaults) {
            Collections.addAll(defaultList, array);
        }
        return defaultList;
    }

    @Override
    public void onEnable() {
        PluginLoader pluginLoader = getPluginLoader();
        File file = new File(getDataFolder(), "schemes.yml");
        try {
            MapSchemeFactory factory = MapSchemeFactory.create(
                MappaInjector.newInjector(new BasicMappaModule()));
            PartInjector partInjector = Commands.newInjector(
                new DefaultsModule(),
                new BukkitModule(),
                new MappaBukkitPartModule(this)
            );
            BukkitCommandManager commandManager = new BukkitCommandManager("mappa");
            initTextHandler(commandManager);
            initTools();

            this.bootstrap = new MappaBootstrap(YamlMapper.newMapper(),
                factory,
                getDataFolder(),
                commandManager,
                partInjector,
                textHandler,
                new Key(MapEditSession.class, Sender.class));
            this.eventBus = bootstrap.getEventBus();
            ConsoleCommandSender sender = Bukkit.getConsoleSender();
            bootstrap.loadSchemes(file, sender);

            ConfigurationSection section = mainConfig.getConfigurationSection("load.map-source");
            if (section != null) {
                Map<String, String> mapSources = new LinkedHashMap<>();
                for (String schemeName : section.getKeys(false)) {
                    String path = section.getString(schemeName);
                    mapSources.put(schemeName, path);
                }
                bootstrap.loadFileSources(sender, mapSources);
            }

            initVisualizer();

            Cache<UUID, Clipboard> clipboard = CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
            this.clipboardHandler = new ClipboardHandlerImpl(clipboard.asMap());
            clipboardHandler.registerTypeTransform(Vector.class, new VectorTransform());
            clipboardHandler.registerTypeTransform(Cuboid.class, new CuboidTransform());

            if (mainConfig.getBoolean("load.resume-all-sessions")) {
                boolean dangerous = mainConfig.getBoolean("load.resume-dangerous-sessions");
                bootstrap.resumeSessions(sender, dangerous);
            }

            AnnotatedCommandTreeBuilder builder = AnnotatedCommandTreeBuilder.create(partInjector);
            this.command = new MappaCommand(this);
            commandManager.registerCommands(builder.fromClass(command));
            eventBus.listen(MappaSavedEvent.class,
                event -> {
                    Object eventSender = event.getSender();
                    if (!(eventSender instanceof Player)) {
                        return;
                    }

                    Player player = (Player) eventSender;
                    MapSession otherSession = bootstrap.getSessionByEntity(player.getUniqueId());
                    String sessionId = event.getMapSessionId();
                    if (!sessionId.equals(otherSession.getId())) {
                        return;
                    }

                    textHandler.send(player,
                        TranslationNode
                            .DESELECTED_SESSION
                            .withFormal("{id}", otherSession));
                });
            eventBus.listen(MappaSetupStepEvent.class,
                event -> {
                    Object eventSender = event.getSender();
                    if (!(eventSender instanceof Player)) {
                        return;
                    }

                    Player player = (Player) eventSender;
                    MapEditSession session = event.getSession();
                    String setup = session.currentSetup();
                    String format = textHandler.format(player,
                        BukkitTranslationNode
                            .SETUP_ACTION_BAR
                            .with("{property}", setup));
                    TextAdapter.sendActionBar(
                        player, TextComponent.of(format));
                });

            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new SelectionListener(this), this);
        } catch (ParseException e) {
            textHandler.send(Bukkit.getConsoleSender(), e.getTextNode());
            e.realStackTrace();
            pluginLoader.disablePlugin(this);
        } catch (IOException e) {
            e.printStackTrace();
            pluginLoader.disablePlugin(this);
        }
    }

    private void initTextHandler(CommandManager commandManager) {
        File file = new File(getDataFolder(), "colors.yml");
        YamlConfiguration colorConfig = YamlConfiguration.loadConfiguration(file);
        Map<ColorScheme, String> colors = new EnumMap<>(ColorScheme.class);
        for (ColorScheme color : ColorScheme.values()) {
            String translated = colorConfig.getString(color.name().toLowerCase());
            colors.put(color, translated);
        }
        String defaultColor = ChatColor.translateAlternateColorCodes(
            '&', colors.get(ColorScheme.BASE));

        this.textHandler = MappaTextHandler.fromSource("en",
            BukkitTranslationNode.PREFIX_PLUGIN.getPath(),
            BUKKIT_SENDER,
            BukkitMessageAdapt.newYamlSource(this),
            handle -> {
                handle.delimiting("{", "}")
                    .addInterceptor(new MappaColorTranslator("$", colors))
                    .addInterceptor(string ->
                        ChatColor.translateAlternateColorCodes('&', string));

                handle.specify(Player.class)
                    .setLinguist(BukkitMessageAdapt.newSpigotLinguist())
                    .setMessageSender((sender, prefix, message) -> sender.sendMessage(prefix + defaultColor + message));

                handle.specify(CommandSender.class)
                    // Sorry yusshu, i have a prefix to concat
                    .setMessageSender((sender, prefix, message) -> sender.sendMessage(prefix + defaultColor + message));

                handle.specify(MapSession.class)
                    .addProvider("session", new MapSessionPlaceholder());

                handle.bindCompatibleSupertype(CommandSender.class, ConsoleCommandSender.class);
            });

        Translator translator = commandManager.getTranslator();
        translator.setProvider(new MessageTranslationProvider("commandflow.", textHandler, BUKKIT_SENDER));

        ErrorHandler errorHandler = commandManager.getErrorHandler();
        errorHandler.addExceptionHandler(ArgumentParseException.class,
            (namespace, throwable) -> {
                CommandSender sender = namespace.getObject(
                    CommandSender.class,
                    BukkitCommandManager.SENDER_NAMESPACE);
                if (!(sender instanceof Player)) {
                    throw throwable;
                }

                String message = throwable.getMessage();
                if (message == null) {
                    return true;
                }

                Component translate = translator.translate(throwable.getMessageComponent(), namespace);
                textHandler.send(sender, Texts.toString(translate), true);
                return true;
            });
        errorHandler.addExceptionHandler(ArgumentTextParseException.class,
            (namespace, throwable) -> {
                CommandSender sender = namespace.getObject(
                    CommandSender.class,
                    BukkitCommandManager.SENDER_NAMESPACE);
                if (!(sender instanceof Player)) {
                    throw throwable;
                }

                Object[] entities = throwable.getEntities();
                textHandler.send(sender, throwable.getText(), entities);
                return true;
            });
        errorHandler.addExceptionHandler(InvalidPropertyException.class,
            (namespace, throwable) -> {
                CommandSender sender = namespace.getObject(
                    CommandSender.class,
                    BukkitCommandManager.SENDER_NAMESPACE);
                if (!(sender instanceof Player)) {
                    throw throwable;
                }

                textHandler.send(sender, throwable.getTextNode());
                return true;
            });
        errorHandler.addExceptionHandler(CommandUsage.class,
            (namespace, throwable) -> {
                CommandSender sender = namespace.getObject(
                    CommandSender.class,
                    BukkitCommandManager.SENDER_NAMESPACE);
                if (!(sender instanceof Player)) {
                    throw throwable;
                }

                String message = "/" + Texts.toString(throwable);
                textHandler.send(sender, message, true);
                return true;
            });
    }

    @SuppressWarnings("UnstableApiUsage")
    private void initTools() {
        this.regionRegistry = new CacheRegionRegistry(
            CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build());
        this.toolHandler = ToolHandler.newToolHandler();
        toolHandler.registerTools(
            new VectorTool(regionRegistry, textHandler),
            new CenteredVectorTool(regionRegistry, textHandler),
            new PreciseVectorTool(projectileCache, regionRegistry, textHandler),
            new RegionRadiusTool(regionRegistry, textHandler),
            new CustomRegionRadiusTool(regionRegistry, textHandler),
            new ArmorStandTool(regionRegistry, textHandler),
            new ScannerVectorTool(this, regionRegistry, textHandler),
            new MirrorVectorTool(regionRegistry, textHandler),
            new YawPitchTool(regionRegistry, textHandler),
            new CenteredYawPitchTool(regionRegistry, textHandler),
            new ChunkTool(regionRegistry, textHandler));
    }

    public void initVisualizer() {
        this.visualizer = new BukkitVisualizer();

        ParticleNativeAPI particleApi = ParticleNativeCore.loadAPI(this);
        Particles_1_8 particles = particleApi.getParticles_1_8();

        visualizer.registerVisual(Vector.class, () -> new VectorRender(particles));
        visualizer.registerVisual(Cuboid.class, () -> new CuboidRender(particles));

        eventBus.listen(MappaRegionSelectEvent.class,
            event -> visualizer.createVisual(
                (Player) event.getSender(), event.getSelection()));
        eventBus.listen(MappaNewSessionEvent.class,
            event -> {
                MapSession mapSession = event.getMapSession();
                if (mapSession instanceof MapEditSession) {
                    visualizer.createVisuals((MapEditSession) mapSession);
                }
            });
        eventBus.listen(MappaPropertySetEvent.class,
            event -> {
                Object entity = event.getEntity();
                for (Text text : event.getMessages()) {
                    textHandler.send(entity, text);
                }
                MapSession mapSession = event.getMapSession();
                if (!(entity instanceof Player) ||
                    !(mapSession instanceof MapEditSession)) {
                    return;
                }

                command.showVisual(
                    (Player) entity,
                    (MapEditSession) mapSession,
                    event.getPath(),
                    event.isSilent());
            });

        this.task = new VisualizerTask(this);
        int frequency = mainConfig.getInt("general.particle-frequency", 15);
        task.start(frequency);
    }

    @Override
    public void onDisable() {
        if (bootstrap == null) {
            return;
        }

        if (visualizer != null) {
            visualizer.unregisterAll();
        }

        if (clipboardHandler != null) {
            clipboardHandler.unregisterAll();
        }

        if (task != null) {
            task.cancel();
        }

        try {
            bootstrap.unload(Bukkit.getConsoleSender(),
                mainConfig.getBoolean("unload.save-ready-sessions")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Consumer<Projectile>> getProjectileCache() {
        return projectileCache;
    }

    @Override
    public MappaBootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    @NotNull
    public ToolHandler getToolHandler() {
        return toolHandler;
    }

    @Override
    public @Nullable ClipboardHandler getClipboardHandler() {
        return clipboardHandler;
    }

    @Override
    public BukkitVisualizer getVisualizer() {
        return visualizer;
    }

    @Override
    @NotNull
    public RegionRegistry getRegionRegistry() {
        return regionRegistry;
    }
}
