package team.unnamed.mappa.bukkit.command;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.bukkit.listener.SelectionListener;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.TextNode;
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

    @Command(names = {"vector-tool", "vector"})
    public void newVectorTool(@Sender Player player) {
        createTool(player,
            ToolHandler.VECTOR_TOOL,
            Material.STICK,
            BukkitTranslationNode.TOOL_VECTOR_NAME);
    }

    @Command(names = {"chunk-tool", "chunk"})
    public void newChunkTool(@Sender Player player) {
        createTool(player,
            ToolHandler.CHUNK_TOOL,
            Material.BLAZE_ROD,
            BukkitTranslationNode.TOOL_CHUNK_NAME);
    }

    @Command(names = {"tool"})
    public void newTool(@Sender Player player,
                        String toolId) {
        createTool(player,
            toolId,
            Material.GOLD_HOE,
            BukkitTranslationNode.TOOL_CUSTOM_NAME);
    }

    public void createTool(Player player,
                           String toolId,
                           Material material,
                           BukkitTranslationNode node) {
        ToolHandler toolHandler = bootstrap.getToolHandler();
        Tool<Player> tool = toolHandler.getToolById(toolId, player);
        if (tool == null) {
            textHandler.send(player,
                BukkitTranslationNode
                    .TOOL_NOT_FOUND
                    .withFormal("id", toolId));
            return;
        }

        ItemStack itemStack = NBTEditor.set(new ItemStack(material), toolId, SelectionListener.TOOL_ID);
        ItemMeta itemMeta = itemStack.getItemMeta();
        TextNode textNode = node.withFormal("{id}", toolId);
        itemMeta.setDisplayName(
            textHandler.format(player, textNode));
        itemStack.setItemMeta(itemMeta);
        PlayerInventory inventory = player.getInventory();
        inventory.addItem(itemStack);

        textHandler.send(player,
            BukkitTranslationNode
                .TOOL_RECEIVED
                .withFormal("{id}", toolId));
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
