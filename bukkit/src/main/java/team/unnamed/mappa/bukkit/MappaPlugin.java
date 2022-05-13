package team.unnamed.mappa.bukkit;

import com.google.common.cache.Cache;
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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.command.MappaCommand;
import team.unnamed.mappa.bukkit.command.part.MappaBukkitPartModule;
import team.unnamed.mappa.bukkit.exception.ArgumentTextParseException;
import team.unnamed.mappa.bukkit.internal.CacheRegionRegistry;
import team.unnamed.mappa.bukkit.internal.GettableTranslationProvider;
import team.unnamed.mappa.bukkit.listener.SelectionListener;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.text.YamlFile;
import team.unnamed.mappa.bukkit.util.BlockUtils;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.bukkit.util.MathUtils;
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
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MappaPlugin extends JavaPlugin {
    public static final EntityProvider BUKKIT_SENDER =
        context -> context.getObject(
            CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);

    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final String DEFAULT_LANGUAGE = "lang_" + DEFAULT_LOCALE;

    private MappaBootstrap bootstrap;
    private MappaTextHandler textHandler;
    private ToolHandler toolHandler;
    private RegionRegistry regionRegistry;
    private final Map<Integer, Consumer<Projectile>> projectiles = new HashMap<>();

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
            DEFAULT_LANGUAGE,
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
            pluginManager.registerEvents(new SelectionListener(toolHandler, projectiles), this);

            AnnotatedCommandTreeBuilder builder = AnnotatedCommandTreeBuilder.create(partInjector);
            commandManager.registerCommands(builder.fromClass(new MappaCommand(this)));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
        }
    }

    private void initTextHandler(CommandManager commandManager) {
        this.textHandler = MappaTextHandler.fromSource(DEFAULT_LOCALE.toString(),
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
                textHandler.send(sender, throwable.getText());
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
        Cache<String, Map<Class<?>, RegionSelection<?>>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
        this.regionRegistry = new CacheRegionRegistry(cache);
        this.toolHandler = ToolHandler.newToolHandler();
        Tool<Player> vectorTool = Tool.newTool(ToolHandler.VECTOR_TOOL, "mappa.tool.vector", false, Player.class);
        Tool<Player> preciseTool = Tool.newTool(ToolHandler.PRECISE_VECTOR_TOOL, "mappa.tool.precise-vector", true, Player.class);
        Tool<Player> yawPitchTool = Tool.newTool(ToolHandler.YAW_PITCH_TOOL, "mappa.tool.yaw-pitch", true, Player.class);
        Tool<Player> chunkTool = Tool.newTool(ToolHandler.CHUNK_TOOL, "mappa.tool.chunk", false, Player.class);

        Tool.Action<Player> vectorAction = (entity, lookingAt, button) -> {
            String uniqueId = entity.getUniqueId().toString();
            RegionSelection<Vector> vectorSelection = regionRegistry.getVectorSelection(uniqueId);
            if (vectorSelection == null) {
                vectorSelection = regionRegistry.newVectorSelection(uniqueId);
            }

            BukkitTranslationNode text;
            if (button == Tool.Button.LEFT) {
                vectorSelection.setFirstPoint(lookingAt);
                text = BukkitTranslationNode.FIRST_POINT_SELECTED;
            } else if (button == Tool.Button.RIGHT) {
                vectorSelection.setSecondPoint(lookingAt);
                text = BukkitTranslationNode.SECOND_POINT_SELECTED;
            } else {
                return;
            }

            TextNode node = text.withFormal(
                "{type}", Texts.getTypeName(Vector.class),
                "{location}", Vector.toString(lookingAt));
            textHandler.send(entity, node);
        };

        vectorTool.registerAction(vectorAction);
        preciseTool.registerAction((entity, lookingAt, button) -> {
            Location location = entity.getLocation();
            Arrow arrow = entity.launchProjectile(Arrow.class, location.getDirection());
            arrow.setCritical(false);
            arrow.spigot().setDamage(0D);
            projectiles.put(arrow.getEntityId(),
                projectile -> {
                    Location arrowLocation = projectile.getLocation();
                    Vector arrowHit = MappaBukkit.toMappa(arrowLocation.toVector());
                    arrowHit = MathUtils.roundVector(arrowHit);

                    if (mainConfig.getBoolean("in-game.block-top-equals-block-location", false)) {
                        Block hitBlock = BlockUtils.getHitBlockOf(arrow);
                        if (hitBlock != null) {
                            Block block = arrowLocation.getBlock();
                            BlockFace face = hitBlock.getFace(block);
                            if (face == BlockFace.UP) {
                                arrowHit = arrowHit.mutY(hitBlock.getY());
                            }
                        }
                    }
                    vectorAction.call(entity,
                        arrowHit,
                        button);
                }
            );
        });

        yawPitchTool
            .registerAction((entity, lookingAt, button) -> {
                String uniqueId = entity.getUniqueId().toString();
                RegionSelection<Vector> vectorSelection = regionRegistry.getVectorSelection(uniqueId);
                if (vectorSelection == null) {
                    vectorSelection = regionRegistry.newVectorSelection(uniqueId);
                }

                BukkitTranslationNode text;
                Vector point;
                String typeName = Texts.getTypeName(Vector.class);
                if (button == Tool.Button.LEFT) {
                    point = vectorSelection.getFirstPoint();
                    if (point == null) {
                        textHandler.send(entity,
                            BukkitTranslationNode
                                .FIRST_POINT_NOT_EXISTS
                                .withFormal("{type}", typeName));
                        return;
                    }
                    text = BukkitTranslationNode.FIRST_YAW_PITCH_SELECTED;
                } else if (button == Tool.Button.RIGHT) {
                    point = vectorSelection.getSecondPoint();
                    if (point == null) {
                        textHandler.send(entity,
                            BukkitTranslationNode
                                .SECOND_POINT_NOT_EXISTS
                                .withFormal("{type}", typeName));
                        return;
                    }
                    text = BukkitTranslationNode.SECOND_YAW_PITCH_SELECTED;
                } else {
                    return;
                }

                Location location = entity.getLocation();
                float yaw = MathUtils.roundDecimals(location.getYaw());
                float pitch = MathUtils.roundDecimals(location.getPitch());
                point.setYaw(yaw);
                point.setPitch(pitch);

                Text node = text.withFormal("{location}", yaw + ", " + pitch);
                textHandler.send(entity, node);
            });

        chunkTool
            .registerAction((entity, lookingAt, button) -> {
                String uniqueId = entity.getUniqueId().toString();
                RegionSelection<Chunk> chunkSelection = regionRegistry.getChunkSelection(uniqueId);
                if (chunkSelection == null) {
                    chunkSelection = regionRegistry.newChunkSelection(uniqueId);
                }

                World world = entity.getWorld();
                Location location = new Location(world, lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
                Chunk chunkMappa = MappaBukkit.toMappa(location.getChunk());
                BukkitTranslationNode text;
                if (button == Tool.Button.LEFT) {
                    chunkSelection.setFirstPoint(chunkMappa);
                    text = BukkitTranslationNode.FIRST_POINT_SELECTED;
                } else if (button == Tool.Button.RIGHT) {
                    chunkSelection.setSecondPoint(chunkMappa);
                    text = BukkitTranslationNode.SECOND_POINT_SELECTED;
                } else {
                    return;
                }

                Text node = text.withFormal(
                    "{type}", Texts.getTypeName(Chunk.class),
                    "{location}", Chunk.toString(chunkMappa));
                textHandler.send(entity, node);
            });

        toolHandler.registerTools(
            vectorTool, preciseTool, yawPitchTool, chunkTool);
    }

    @Override
    public void onDisable() {
        try {
            bootstrap.unload(Bukkit.getConsoleSender(),
                mainConfig.getBoolean("unload.save-ready-sessions")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MappaBootstrap getBootstrap() {
        return bootstrap;
    }

    public ToolHandler getToolHandler() {
        return toolHandler;
    }

    public RegionRegistry getRegionRegistry() {
        return regionRegistry;
    }
}
