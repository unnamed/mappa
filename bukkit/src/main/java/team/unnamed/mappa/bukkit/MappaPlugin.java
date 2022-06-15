package team.unnamed.mappa.bukkit;

import com.cryptomorin.xseries.XSound;
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
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.mappa.MappaAPI;
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

public class MappaPlugin extends JavaPlugin implements MappaAPI {
    public static final EntityProvider BUKKIT_SENDER =
        context -> context.getObject(
            CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);

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
            pluginManager.registerEvents(new SelectionListener(toolHandler, projectiles), this);

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
        Tool<Player> centeredVectorTool = Tool.newTool(ToolHandler.CENTERED_VECTOR_TOOL, "mappa.tool.centered-vector", false, Player.class);
        Tool<Player> preciseTool = Tool.newTool(ToolHandler.PRECISE_VECTOR_TOOL, "mappa.tool.precise-vector", true, Player.class);
        Tool<Player> centeredYawPitchTool = Tool.newTool(ToolHandler.CENTERED_YAW_PITCH_TOOL, "mappa.tool.centered-yaw-pitch", true, Player.class);
        Tool<Player> yawPitchTool = Tool.newTool(ToolHandler.YAW_PITCH_TOOL, "mappa.tool.yaw-pitch", true, Player.class);
        Tool<Player> chunkTool = Tool.newTool(ToolHandler.CHUNK_TOOL, "mappa.tool.chunk", false, Player.class);

        Tool.Action<Player> vectorAction = (entity, lookingAt, button, shift) -> {
            String uniqueId = entity.getUniqueId().toString();
            RegionSelection<Vector> vectorSelection = regionRegistry.getVectorSelection(uniqueId);
            if (vectorSelection == null) {
                vectorSelection = regionRegistry.newVectorSelection(uniqueId);
            }

            BukkitTranslationNode text;
            float soundPitch;
            if (shift) {
                int floor = (int) lookingAt.getY();
                lookingAt = lookingAt.mutY(++floor);
            }
            if (button == Tool.Button.RIGHT) {
                vectorSelection.setFirstPoint(lookingAt);
                text = shift
                    ? BukkitTranslationNode.FIRST_POINT_FLOOR_SELECTED
                    : BukkitTranslationNode.FIRST_POINT_SELECTED;
                soundPitch = 0.5F;
            } else if (button == Tool.Button.LEFT) {
                vectorSelection.setSecondPoint(lookingAt);
                text = shift
                    ? BukkitTranslationNode.SECOND_POINT_FLOOR_SELECTED
                    : BukkitTranslationNode.SECOND_POINT_SELECTED;
                soundPitch = 1.0F;
            } else {
                return;
            }
            TextNode node = text.with(
                "{type}", Texts.getTypeName(Vector.class),
                "{location}", Vector.toString(lookingAt));
            textHandler.send(entity, node);
            XSound.UI_BUTTON_CLICK.play(entity, 1.0F, soundPitch);
        };

        vectorTool.registerAction(vectorAction);
        centeredVectorTool.registerAction((entity, lookingAt, button, shift) -> {
            lookingAt = lookingAt.sum(0.5, 0, 0.5);
            vectorAction.call(entity, lookingAt, button, shift);
        });
        preciseTool.registerAction((entity, lookingAt, button, shift) -> {
            Location location = entity.getLocation();
            Arrow arrow = entity.launchProjectile(Arrow.class, location.getDirection());
            arrow.setCritical(false);
            arrow.spigot().setDamage(0D);
            projectiles.put(arrow.getEntityId(),
                projectile -> {
                    Location arrowLocation = projectile.getLocation();
                    Vector arrowHit = MappaBukkit.toMappa(arrowLocation.toVector());
                    arrowHit = MathUtils.roundVector(arrowHit);

                    if (shift) {
                        Block hitBlock = BlockUtils.getHitBlockOf(arrow);
                        if (hitBlock != null) {
                            Block block = arrowLocation.getBlock();
                            BlockFace face = hitBlock.getFace(block);
                            if (face == BlockFace.UP) {
                                int y = hitBlock.getY();
                                arrowHit = arrowHit.mutY(++y); // ++y to block floor
                            }
                        }
                    }
                    vectorAction.call(entity,
                        arrowHit,
                        button,
                        false); // We don't use shift because here we already used
                }
            );
        });

        Tool.Action<Player> yawPitchAction = (entity, lookingAt, button, shift) -> {
            String uniqueId = entity.getUniqueId().toString();
            RegionSelection<Vector> vectorSelection = regionRegistry.getVectorSelection(uniqueId);
            if (vectorSelection == null) {
                vectorSelection = regionRegistry.newVectorSelection(uniqueId);
            }

            BukkitTranslationNode text;
            Vector point;
            String typeName = Texts.getTypeName(Vector.class);
            float soundPitch;
            if (button == Tool.Button.RIGHT) {
                point = vectorSelection.getFirstPoint();
                if (point == null) {
                    textHandler.send(entity,
                        BukkitTranslationNode
                            .FIRST_POINT_NOT_EXISTS
                            .with("{type}", typeName));
                    return;
                }

                // Where is my boilerplate?!!
                double yaw = MathUtils.fixYaw(lookingAt.getYaw());
                double pitch = lookingAt.getPitch();
                point = point.mutYawPitch(yaw, pitch);
                vectorSelection.setFirstPoint(point);
                text = BukkitTranslationNode.FIRST_YAW_PITCH_SELECTED;
                soundPitch = 0.5F;
            } else if (button == Tool.Button.LEFT) {
                point = vectorSelection.getSecondPoint();
                if (point == null) {
                    textHandler.send(entity,
                        BukkitTranslationNode
                            .SECOND_POINT_NOT_EXISTS
                            .with("{type}", typeName));
                    return;
                }

                double yaw = MathUtils.fixYaw(lookingAt.getYaw());
                double pitch = lookingAt.getPitch();
                point = point.mutYawPitch(yaw, pitch);
                vectorSelection.setSecondPoint(point);
                text = BukkitTranslationNode.SECOND_YAW_PITCH_SELECTED;
                soundPitch = 1.0F;
            } else {
                return;
            }

            Text node = text.with("{location}", point.getYaw() + ", " + point.getPitch());
            textHandler.send(entity, node);
            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(entity, 1.0F, soundPitch);
        };
        yawPitchTool.registerAction((entity, lookingAt, button, shift) -> {
            lookingAt = MappaBukkit.toMappa(entity.getLocation());
            lookingAt = MathUtils.roundVector(lookingAt);
            yawPitchAction.call(entity,
                lookingAt,
                button,
                shift);
        });
        centeredYawPitchTool.registerAction((entity, lookingAt, button, shift) -> {
            lookingAt = MappaBukkit.toMappa(entity.getLocation());
            double yaw = MathUtils.roundAllDecimals(lookingAt.getYaw());
            double pitch = MathUtils.roundAllDecimals(lookingAt.getPitch());
            lookingAt = lookingAt.mutYawPitch(yaw, pitch);
            yawPitchAction.call(entity, lookingAt, button, shift);
        });

        chunkTool
            .registerAction((entity, lookingAt, button, shift) -> {
                String uniqueId = entity.getUniqueId().toString();
                RegionSelection<Chunk> chunkSelection = regionRegistry.getChunkSelection(uniqueId);
                if (chunkSelection == null) {
                    chunkSelection = regionRegistry.newChunkSelection(uniqueId);
                }

                World world = entity.getWorld();
                Location location = new Location(world, lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
                Chunk chunkMappa = MappaBukkit.toMappa(location.getChunk());
                BukkitTranslationNode text;
                float soundPitch;
                if (button == Tool.Button.RIGHT) {
                    chunkSelection.setFirstPoint(chunkMappa);
                    text = BukkitTranslationNode.FIRST_POINT_SELECTED;
                    soundPitch = 0.5F;
                } else if (button == Tool.Button.LEFT) {
                    chunkSelection.setSecondPoint(chunkMappa);
                    text = BukkitTranslationNode.SECOND_POINT_SELECTED;
                    soundPitch = 1.0F;
                } else {
                    return;
                }

                Text node = text.with(
                    "{type}", Texts.getTypeName(Chunk.class),
                    "{location}", Chunk.toString(chunkMappa));
                textHandler.send(entity, node);
                XSound.BLOCK_NOTE_BLOCK_BASS.play(entity, 1.0F, soundPitch);
            });

        toolHandler.registerTools(
            vectorTool,
            centeredVectorTool,
            preciseTool,
            yawPitchTool,
            centeredYawPitchTool,
            chunkTool);
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

    @Override
    public MappaBootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public ToolHandler getToolHandler() {
        return toolHandler;
    }

    @Override
    public RegionRegistry getRegionRegistry() {
        return regionRegistry;
    }
}
