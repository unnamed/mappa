package team.unnamed.mappa.bukkit;

import com.google.common.cache.CacheBuilder;
import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.ErrorHandler;
import me.fixeddev.commandflow.annotated.AnnotatedCommandTreeBuilder;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.factory.BukkitModule;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.exception.CommandUsage;
import me.fixeddev.commandflow.translator.Translator;
import me.yushust.message.bukkit.BukkitMessageAdapt;
import net.kyori.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.command.MappaCommand;
import team.unnamed.mappa.bukkit.command.part.MappaBukkitPartModule;
import team.unnamed.mappa.bukkit.internal.CacheRegionRegistry;
import team.unnamed.mappa.bukkit.internal.GettableTranslationProvider;
import team.unnamed.mappa.bukkit.listener.SelectionListener;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.text.YamlFile;
import team.unnamed.mappa.bukkit.tool.*;
import team.unnamed.mappa.bukkit.util.Texts;
import team.unnamed.mappa.function.EntityProvider;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.injector.BasicMappaModule;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.message.MessageTranslationProvider;
import team.unnamed.mappa.internal.message.placeholder.MapSessionPlaceholder;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.object.TextDefault;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;
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

    private MappaBootstrap bootstrap;
    private MappaTextHandler textHandler;
    private ToolHandler toolHandler;
    private RegionRegistry regionRegistry;

    private final Map<Integer, Consumer<Projectile>> projectileCache = new HashMap<>();
    private final Map<UUID, String> playerToSession = new WeakHashMap<>();

    private FileConfiguration mainConfig;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        this.mainConfig = getConfig();
        File schemes = new File(getDataFolder(), "schemes.yml");
        if (!schemes.exists()) {
            saveResource("schemes.yml", false);
        }

        List<TextDefault> list = asTranslation(TranslationNode.values(), BukkitTranslationNode.values());
        GettableTranslationProvider provider = new GettableTranslationProvider();
        list.addAll(provider.toTexts("commandflow."));

        YamlFile.refillFileWith(
            this,
            "lang_US",
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
                textHandler);
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

            if (mainConfig.getBoolean("load.resume-all-sessions")) {
                boolean dangerous = mainConfig.getBoolean("load.resume-dangerous-sessions");
                bootstrap.resumeSessions(sender, dangerous);
            }

            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new SelectionListener(this), this);

            AnnotatedCommandTreeBuilder builder = AnnotatedCommandTreeBuilder.create(partInjector);
            commandManager.registerCommands(builder.fromClass(new MappaCommand(this)));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            pluginLoader.disablePlugin(this);
        }
    }

    private void initTextHandler(CommandManager commandManager) {
        this.textHandler = MappaTextHandler.fromSource("US",
            BukkitTranslationNode.PREFIX_PLUGIN.getPath(),
            BUKKIT_SENDER,
            BukkitMessageAdapt.newYamlSource(this),
            handle -> {
                handle.delimiting("{", "}")
                    .addInterceptor(string ->
                        ChatColor.translateAlternateColorCodes('&', string));

                handle.specify(Player.class)
                    .setLinguist(BukkitMessageAdapt.newSpigotLinguist())
                    .setMessageSender((sender, prefix, message) -> sender.sendMessage(prefix + message));

                handle.specify(CommandSender.class)
                    // Sorry yusshu, i have a prefix to concat
                    .setMessageSender((sender, prefix, message) -> sender.sendMessage(prefix + message));

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
                textHandler.send(sender, throwable.getText(), throwable.getEntities());
                return true;
            });
        errorHandler.addExceptionHandler(InvalidPropertyException.class,
            (namespace, throwable) -> {
                CommandSender sender = namespace.getObject(
                    CommandSender.class,
                    BukkitCommandManager.SENDER_NAMESPACE);
                textHandler.send(sender, throwable.getTextNode());
                return true;
            });
        errorHandler.addExceptionHandler(CommandUsage.class,
            (namespace, throwable) -> {
                CommandSender sender = namespace.getObject(
                    CommandSender.class,
                    BukkitCommandManager.SENDER_NAMESPACE);

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
            new ScannerVectorTool(this, playerToSession, regionRegistry, textHandler),
            new MirrorVectorTool(regionRegistry, textHandler),
            new YawPitchTool(regionRegistry, textHandler),
            new CenteredYawPitchTool(regionRegistry, textHandler),
            new ChunkTool(regionRegistry, textHandler));
    }

    @Override
    public void onDisable() {
        if (bootstrap == null) {
            return;
        }

        try {
            bootstrap.unload(Bukkit.getConsoleSender(),
                mainConfig.getBoolean("unload.save-ready-sessions")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, String> getPlayerToSession() {
        return playerToSession;
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
    @NotNull
    public RegionRegistry getRegionRegistry() {
        return regionRegistry;
    }
}
