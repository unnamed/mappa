package team.unnamed.mappa.bukkit;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.fixeddev.commandflow.annotated.AnnotatedCommandTreeBuilder;
import me.fixeddev.commandflow.annotated.builder.AnnotatedCommandBuilderImpl;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.factory.BukkitModule;
import me.yushust.message.bukkit.BukkitMessageAdapt;
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
import team.unnamed.mappa.Mappa;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaPlatform;
import team.unnamed.mappa.MappaPlatformBuilder;
import team.unnamed.mappa.bukkit.command.MappaCommand;
import team.unnamed.mappa.bukkit.internal.BukkitPlayerRegistry;
import team.unnamed.mappa.bukkit.internal.GettableTranslationProvider;
import team.unnamed.mappa.bukkit.listener.SelectionListener;
import team.unnamed.mappa.bukkit.render.CuboidRender;
import team.unnamed.mappa.bukkit.render.VectorRender;
import team.unnamed.mappa.bukkit.text.YamlFile;
import team.unnamed.mappa.bukkit.tool.*;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.clipboard.ClipboardHandlerImpl;
import team.unnamed.mappa.internal.color.ColorScheme;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.command.ReflectionMappaInstanceCreator;
import team.unnamed.mappa.internal.command.parts.MappaAPIPartModule;
import team.unnamed.mappa.internal.event.MappaNewSessionEvent;
import team.unnamed.mappa.internal.event.MappaPropertySetEvent;
import team.unnamed.mappa.internal.event.MappaRegionSelectEvent;
import team.unnamed.mappa.internal.event.MappaSetupStepEvent;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.message.MappaColorTranslator;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.message.placeholder.MapSessionPlaceholder;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.model.visualizer.DefaultVisualizer;
import team.unnamed.mappa.model.visualizer.Visualizer;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MappaPlugin extends JavaPlugin implements MappaAPI {

    private MappaPlatform platform;
    private EventBus eventBus;
    private MappaTextHandler textHandler;
    private ToolHandler toolHandler;
    private RegionRegistry regionRegistry;
    private DefaultVisualizer visualizer;
    private BukkitPlayerRegistry playerRegistry;
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
        eventBus = new EventBus();
        playerRegistry = new BukkitPlayerRegistry(this);
        try {
            PartInjector partInjector = Commands.newInjector(
                new DefaultsModule(),
                new BukkitModule(),
                new MappaAPIPartModule(this)
            );
            BukkitCommandManager commandManager = new BukkitCommandManager("mappa");
            initTextHandler();
            initTools();
            initVisualizer();

            Cache<UUID, Clipboard> clipboard = CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
            this.clipboardHandler = new ClipboardHandlerImpl(clipboard.asMap());

            MappaPlatformBuilder platformBuilder = MappaPlatform.builder(this)
                .dataFolder(getDataFolder())
                .mapper(YamlMapper.newMapper())
                .commandManager(commandManager, partInjector);
            this.platform = Mappa.init(this, platformBuilder);

            AnnotatedCommandTreeBuilder builder = AnnotatedCommandTreeBuilder.create(
                new AnnotatedCommandBuilderImpl(partInjector),
                new ReflectionMappaInstanceCreator(this));
            commandManager.registerCommands(builder.fromClass(new MappaCommand(this)));
            eventBus.listen(MappaSetupStepEvent.class,
                event -> {
                    MappaPlayer player = event.getPlayer();
                    if (player.isConsole()) {
                        return;
                    }

                    MapSession session = event.getSession();
                    String setup = session.currentSetup();
                    String format = textHandler.format(player,
                        BukkitTranslationNode
                            .SETUP_ACTION_BAR
                            .with("{property}", setup));
                    TextAdapter.sendActionBar(
                        (Player) player.cast(),
                        TextComponent.of(format));
                });

            MappaPlayer console = playerRegistry.console();
            platform.loadMapScheme(console, file);

            ConfigurationSection section = mainConfig.getConfigurationSection("load.map-source");
            if (section != null) {
                Map<String, String> mapSources = new LinkedHashMap<>();
                for (String schemeName : section.getKeys(false)) {
                    String path = section.getString(schemeName);
                    mapSources.put(schemeName, path);
                }
                platform.loadFileSources(console, mapSources);
            }

            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new SelectionListener(this), this);
        } catch (ParseException e) {
            textHandler.send(Bukkit.getConsoleSender(), e.getTextNode());
            e.realStackTrace();
            pluginLoader.disablePlugin(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initTextHandler() {
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
            TranslationNode.PREFIX_PLUGIN.getPath(),
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
    }

    @SuppressWarnings("UnstableApiUsage")
    private void initTools() {
        Cache<String, Map<Class<?>, RegionSelection<?>>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
        this.regionRegistry = RegionRegistry.newRegistry(cache.asMap());
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
        this.visualizer = new DefaultVisualizer();

        ParticleNativeAPI particleApi = ParticleNativeCore.loadAPI(this);
        Particles_1_8 particles = particleApi.getParticles_1_8();

        visualizer.registerVisual(Vector.class, () -> new VectorRender(particles));
        visualizer.registerVisual(Cuboid.class, () -> new CuboidRender(particles));

        eventBus.listen(MappaRegionSelectEvent.class,
            event -> visualizer.createVisual(
                event.getPlayer(), event.getSelection()));
        eventBus.listen(MappaNewSessionEvent.class,
            event -> visualizer.createVisuals(event.getMapSession()));
        eventBus.listen(MappaPropertySetEvent.class,
            event -> {
                MappaPlayer player = event.getPlayer();
                player.showVisual(event.getPath(), event.isSilent());
            });

        this.task = new VisualizerTask(this);
        int frequency = mainConfig.getInt("general.particle-frequency", 15);
        task.start(frequency);
    }

    @Override
    public void onDisable() {
        if (platform == null) {
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

        eventBus.clearAll();

        try {
            platform.unload(playerRegistry.console());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Integer, Consumer<Projectile>> getProjectileCache() {
        return projectileCache;
    }

    @Override
    public MappaPlatform getPlatform() {
        return platform;
    }

    @Override
    public @NotNull EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public @NotNull MappaTextHandler getTextHandler() {
        return textHandler;
    }

    @Override
    @NotNull
    public ToolHandler getToolHandler() {
        return toolHandler;
    }

    @Override
    public @NotNull ClipboardHandler getClipboardHandler() {
        return clipboardHandler;
    }

    @Override
    public Visualizer getVisualizer() {
        return visualizer;
    }

    @Override
    public boolean initApi() {
        return platform != null;
    }

    @Override
    @NotNull
    public RegionRegistry getRegionRegistry() {
        return regionRegistry;
    }

    @Override
    public @NotNull BukkitPlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }
}
