package team.unnamed.mappa.bukkit.command;

import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.util.List;

@Command(
    names = {"mappa", "map"}
)
public class MappaCommand implements CommandClass {
    private final MappaPlugin plugin;
    private final MappaBootstrap bootstrap;
    private final MappaTextHandler textHandler;

    public MappaCommand(MappaPlugin plugin) {
        this.plugin = plugin;
        this.bootstrap = plugin.getBootstrap();
        this.textHandler = bootstrap.getTextHandler();
    }

    @Command(names = "load")
    public void loadSessions(CommandSender sender,
                            MapScheme scheme,
                            File sessionFile) throws ParseException {
        bootstrap.loadSessions(scheme, sessionFile, sender);
    }

    @Command(names = {"new-session", "new"})
    public void newSession(CommandSender sender,
                           MapScheme scheme,
                           World world) {
        MapSession session = bootstrap.newSession(scheme, world.getName());
        textHandler.send(sender, TranslationNode.NEW_SESSION.withFormal(
            "{map_name}", world.getName(),
            "{map_scheme}", scheme.getName()
        ));
    }

    @Command(names = {"version", "v"})
    public void showVersion(CommandSender sender) {
        PluginDescriptionFile description = plugin.getDescription();
        List<String> authors = description.getAuthors();
        String version = description.getVersion();
        textHandler.send(sender,
            BukkitTranslationNode
                .VERSION_PLUGIN
                .withFormal(
                    "{version}", version,
                    "{author}", String.join(",", authors)
                )
        );
    }

    @Command(names = {"info", "show-info"})
    public void showInfo(CommandSender sender,
                         @Sender World world) {
        List<MapSession> sessions = bootstrap.getSessions(world.getName());
        textHandler.send(sender,
            BukkitTranslationNode
                .SESSION_LIST_HEADER
                .withFormal(
                "{number}", sessions.size()
            ));
        for (int i = 0; i < sessions.size(); i++) {
            MapSession session = sessions.get(i);
            textHandler.send(sender,
                BukkitTranslationNode
                    .SESSION_LIST_ENTRY
                    .withFormal(
                        "{number}", i,
                        "{map_scheme}", session.getSchemeName()
                    ));
        }
    }
}
