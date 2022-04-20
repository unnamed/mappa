package team.unnamed.mappa.bukkit.command;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
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
import team.unnamed.mappa.bukkit.util.CommandBukkit;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Collection;
import java.util.List;

@Command(
    names = {"mappa", "map"}
)
public class MappaCommand implements CommandClass {
    public static final int MAX_FAIL_ENTRY = 8;

    private final MappaPlugin plugin;
    private final MappaBootstrap bootstrap;
    private final MappaTextHandler textHandler;

    public MappaCommand(MappaPlugin plugin) {
        this.plugin = plugin;
        this.bootstrap = plugin.getBootstrap();
        this.textHandler = bootstrap.getTextHandler();
    }

    @Command(names = "verify")
    public void verify(CommandSender sender,
                       MapSession session) throws ParseException {
        List<Text> errorMessages = session.checkWithScheme(false);
        if (errorMessages != null) {
            textHandler.send(sender,
                TranslationNode
                .VERIFY_SESSION_FAIL
                .withFormal(
                    "{session_id}", session.getId(),
                    "{number}", errorMessages.size())
            );
            int i = 0;
            for (Text text : errorMessages) {
                String errMessage = textHandler.format(sender, text);
                textHandler.send(sender,
                    TranslationNode
                        .VERIFY_SESSION_FAIL_ENTRY
                        .with("{error}", errMessage));
                ++i;
                if (i == MAX_FAIL_ENTRY) {
                    textHandler.send(sender,
                        TranslationNode
                            .VERIFY_SESSION_FAIL_SHORTCUT
                            .with("{number}", errorMessages.size() - i));
                    break;
                }
            }
        } else {
            textHandler.send(sender,
                TranslationNode
                    .VERIFY_SESSION_SUCCESS
                    .formalText()
            );
        }
    }

    @Command(names = "resume-sessions")
    public void resume(CommandSender sender) throws ParseException {
        bootstrap.resumeSessions(sender);
    }

    @Command(names = "load")
    public void loadSessions(CommandSender sender,
                             MapScheme scheme) throws ParseException {
        bootstrap.loadSessions(scheme, sender);
    }

    @Command(names = {"new-session", "new"})
    public void newSession(CommandSender sender,
                           MapScheme scheme) {
        MapSession session = bootstrap.newSession(scheme);
        textHandler.send(sender,
            TranslationNode
                .NEW_SESSION
                .withFormal(
                    "{session_id}", session.getId(),
                    "{map_scheme}", scheme.getName()
                )
        );

        setupProperty(sender, session, null);
    }

    @Command(names = "setup")
    public void setupProperty(CommandSender sender,
                              MapSession session,
                              @OptArg("") String arg) {
        if (!session.setup()) {
            textHandler.send(sender, BukkitTranslationNode.NO_SETUP.formalText());
            return;
        }

        String setupStep = session.currentSetup();
        if (arg == null) {
            TextNode header = BukkitTranslationNode
                .SETUP_HEADER
                .with(
                    "{session_id}", session.getId()
                );
            textHandler.send(sender, header);
            textHandler.send(sender,
                BukkitTranslationNode
                    .PROPERTY_NOT_SET
                    .with(
                        "{property}", setupStep
                    ));
            TextNode text = BukkitTranslationNode
                .SETUP_PROPERTY_SET
                .text();

            if (sender instanceof Player) {
                TextComponent component = new TextComponent(
                    textHandler.format(sender, text));

                ClickEvent clickEvent = new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND, "/mappa setup ");
                component.setClickEvent(clickEvent);

                String hover = textHandler.format(sender,
                    BukkitTranslationNode
                        .SETUP_PROPERTY_SET_HOVER
                        .text());
                TextComponent hoverComponent = new TextComponent(hover);
                HoverEvent hoverEvent = new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hoverComponent});
                component.setHoverEvent(hoverEvent);

                Player player = (Player) sender;
                player.spigot().sendMessage(component);
            } else {
                textHandler.send(sender, text);
            }
            textHandler.send(sender, header);
            return;
        }

        String line = session.getSchemeName()
            + " "
            + setupStep.replace(".", " ")
            + " ";
        if (!arg.isEmpty()) {
            line += arg + " ";
        }
        line += session.getId();
        try {
            CommandBukkit.execute(bootstrap.getCommandManager(),
                namespace -> {
                    namespace.setObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE, sender);
                    int beginIndex = setupStep.lastIndexOf(".");
                    String name;
                    if (beginIndex != -1) {
                        name = setupStep.substring(beginIndex);
                    } else {
                        name = setupStep;
                    }
                    namespace.setObject(String.class, "label", name);
                },
                line);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        setupProperty(sender, session, null);
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

    @Command(names = {"list", "ls", "sessions"})
    public void showSessions(CommandSender sender) {
        Collection<MapSession> sessions = bootstrap.getSessions();
        if (sessions == null) {
            textHandler.send(sender,
                BukkitTranslationNode
                    .SESSION_LIST_EMPTY
                    .formalText());
            return;
        }
        textHandler.send(sender,
            BukkitTranslationNode
                .SESSION_LIST_HEADER
                .withFormal(
                    "{number}", sessions.size()
                ));

        int i = 0;
        for (MapSession session : sessions) {
            textHandler.send(sender,
                BukkitTranslationNode
                    .SESSION_LIST_ENTRY
                    .withFormal(
                        "{number}", ++i,
                        "{map_scheme}", session.getSchemeName()
                    ));
        }
    }
}
