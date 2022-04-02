package team.unnamed.mappa.bukkit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.factory.BukkitModule;
import me.yushust.message.bukkit.BukkitMessageAdapt;
import me.yushust.message.source.MessageSource;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.command.part.MappaBukkitPartModule;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.text.YamlFile;
import team.unnamed.mappa.function.EntityProvider;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.injector.BasicMappaModule;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.TextDefault;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MappaPlugin extends JavaPlugin {
    public static final EntityProvider BUKKIT_SENDER =
        context -> context.getObject(
            CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);

    private final MappaBootstrap bootstrap;

    @SuppressWarnings("UnstableApiUsage")
    public MappaPlugin() throws ParseException {
        BukkitCommandManager commandManager = new BukkitCommandManager("mappa");
        MapSchemeFactory factory = MapSchemeFactory.create(
            MappaInjector.newInjector(new BasicMappaModule()));
        PartInjector partInjector = Commands.newInjector(
            new DefaultsModule(),
            new BukkitModule(),
            new MappaBukkitPartModule(this)
        );

        MessageSource messageSource = BukkitMessageAdapt.newYamlSource(this);
        MappaTextHandler mappaTextHandler = MappaTextHandler.fromSource(
            messageSource,
            handle -> {
                handle.specify(Player.class)
                    .setLinguist(BukkitMessageAdapt.newSpigotLinguist());

                handle.specify(CommandSender.class)
                    .setMessageSender((sender, mode, message) -> sender.sendMessage(message));

                handle.bindCompatibleSupertype(CommandSender.class, ConsoleCommandSender.class);
                handle.bindCompatibleSupertype(CommandSender.class, Player.class);
            });

        Cache<String, Map<Class<?>, RegionSelection<?>>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .weakKeys()
            .build();
        RegionRegistry regionRegistry = RegionRegistry.newRegistry(cache.asMap());

        this.bootstrap = new MappaBootstrap(
            YamlMapper.newMapper(),
            factory,
            commandManager,
            mappaTextHandler,
            regionRegistry,
            partInjector,
            BUKKIT_SENDER);
    }

    @Override
    public void onLoad() {
        saveResource("schemes.yml", false);

        YamlFile.refillFileWith(
            this,
            "lang_ " + Locale.US,
            asTranslation(TranslationNode.values(), BukkitTranslationNode.values())
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
            bootstrap.load(file, Bukkit.getConsoleSender());
        } catch (ParseException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        bootstrap.unload();
    }

    public MappaBootstrap getBootstrap() {
        return bootstrap;
    }
}
