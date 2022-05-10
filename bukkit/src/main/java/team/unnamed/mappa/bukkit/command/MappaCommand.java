package team.unnamed.mappa.bukkit.command;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.ErrorHandler;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.bukkit.exception.ArgumentTextParseException;
import team.unnamed.mappa.bukkit.listener.SelectionListener;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.util.CommandBukkit;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Command(
    names = {"mappa", "map"}
)
public class MappaCommand implements CommandClass {
    public static final int MAX_FAIL_ENTRY = 8;

    private final MappaPlugin plugin;
    private final MappaBootstrap bootstrap;
    private final MappaTextHandler textHandler;
    private final ErrorHandler errorHandler;

    public MappaCommand(MappaPlugin plugin) {
        this.plugin = plugin;
        this.bootstrap = plugin.getBootstrap();
        this.textHandler = bootstrap.getTextHandler();
        this.errorHandler = bootstrap.getCommandManager().getErrorHandler();
    }

    @Command(names = "verify")
    public void verify(CommandSender sender,
                       @Switch("all") boolean showAll,
                       MapSession session) {
        Map<String, Text> errorMessages = session.checkWithScheme(false);
        if (!errorMessages.isEmpty()) {
            textHandler.send(sender,
                TranslationNode
                    .VERIFY_SESSION_FAIL
                    .withFormal("{number}", errorMessages.size()),
                session);
            int i = 0;
            for (Map.Entry<String, Text> entry : errorMessages.entrySet()) {
                Text text = entry.getValue();
                String error = textHandler.format(sender, text);
                textHandler.send(sender,
                    TranslationNode
                        .VERIFY_SESSION_FAIL_ENTRY
                        .with("{property}", entry.getKey(),
                            "{error}", error));
                ++i;
                if (i == MAX_FAIL_ENTRY && !showAll) {
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
                    .formalText(),
                sender
            );
        }
    }

    @Command(names = "resume-sessions")
    public void resume(CommandContext context,
                       CommandSender sender) throws Throwable {
        try {
            bootstrap.resumeSessions(sender, true);
        } catch (Exception e) {
            errorHandler.handleException(context, e);
        }
    }

    @Command(names = "load")
    public void loadSessions(CommandSender sender,
                             MapScheme scheme) throws ParseException {
        bootstrap.loadSessions(scheme, sender);
    }

    @Command(names = {"resume-serialized"})
    public void resumeSerializedSession(CommandContext context,
                                        CommandSender sender,
                                        MapSerializedSession serializedSession) throws Throwable {
        try {
            bootstrap.resumeSession(sender, serializedSession);
        } catch (Exception e) {
            errorHandler.handleException(context, e);
        }
    }

    @Command(names = {"new-session", "new"})
    public void newSession(CommandSender sender,
                           MapScheme scheme,
                           @OptArg("") String id) {
        MapSession session = id.isEmpty()
            ? bootstrap.newSession(scheme)
            : bootstrap.newSession(scheme, id);
        if (session == null) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SESSION_ALREADY_EXISTS
                    .withFormal("{id}", id));
        }
        textHandler.send(sender,
            TranslationNode
                .NEW_SESSION
                .formalText(),
            session
        );

        if (!(sender instanceof Player)) {
            return;
        }

        setupProperty((Player) sender, session, null);
    }

    @Command(names = {"skip-setup", "skip"})
    public void skipSetupProperty(@Sender Player sender,
                                  MapSession session) {
        String setupStep = session.currentSetup();
        MapProperty property = session.getProperty(setupStep);
        if (!property.isOptional()) {
            textHandler.send(sender, BukkitTranslationNode.NO_OPTIONAL_SETUP.formalText());
            return;
        }

        session.skipSetup();
        sender.sendMessage(" ");
        setupProperty(sender, session, null);
    }

    @Command(names = "setup")
    public void setupProperty(@Sender Player sender,
                              MapSession session,
                              @OptArg("") String arg) {
        if (!session.setup()) {
            textHandler.send(sender, BukkitTranslationNode.NO_SETUP.formalText());
            return;
        }

        String setupStep = session.currentSetup();
        String sessionId = session.getId();
        String line = session.getSchemeName()
            + " "
            + setupStep.replace(".", " ")
            + " "
            + sessionId;
        if (arg == null) {
            Text header = BukkitTranslationNode
                .SETUP_HEADER
                .with(
                    "{session_id}", sessionId
                );
            textHandler.send(sender, header);
            Text defineText = BukkitTranslationNode
                .DEFINE_PROPERTY
                .with("{property}", setupStep);
            Text lineText = BukkitTranslationNode
                .SETUP_PROPERTY_SET
                .text();
            MapProperty property = session.getProperty(setupStep);
            Text optionalText = property.isOptional()
                ? BukkitTranslationNode.PROPERTY_SKIP_SETUP.text()
                : null;

            Player.Spigot spigot = sender.spigot();
            spigot.sendMessage(commandComponent(
                sender,
                defineText,
                BukkitTranslationNode.VIEW_PROPERTY_SET_HOVER.formalText(),
                ClickEvent.Action.RUN_COMMAND,
                line + " -v " + sessionId));

            spigot.sendMessage(commandComponent(
                sender, lineText, "mappa setup " + sessionId + " "));

            if (optionalText != null) {
                sender.sendMessage(" ");
                spigot.sendMessage(commandComponent(
                    sender, optionalText, "mappa skip-setup " + sessionId));
            }

            Map<String, Text> errors = session.checkWithScheme(true);
            if (errors.isEmpty()) {
                textHandler.send(sender, BukkitTranslationNode.SETUP_READY.text());
            }
            textHandler.send(sender, header);
            return;
        }

        if (!arg.isEmpty()) {
            line += " " + arg;
        }
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

        sender.sendMessage(" ");
        setupProperty(sender, session, null);
    }

    public TextComponent commandComponent(Object sender,
                                          Text message,
                                          Text hoverText,
                                          ClickEvent.Action clickAction,
                                          String command) {
        TextComponent component = new TextComponent(
            textHandler.format(sender, message));

        ClickEvent clickEvent = new ClickEvent(clickAction, "/" + command);
        component.setClickEvent(clickEvent);

        String hover = textHandler.format(sender, hoverText);
        TextComponent hoverComponent = new TextComponent(hover);
        HoverEvent hoverEvent = new HoverEvent(
            HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hoverComponent});
        component.setHoverEvent(hoverEvent);
        return component;
    }

    public TextComponent commandComponent(Object sender, Text message, Text hoverText, String command) {
        return commandComponent(sender,
            message,
            hoverText,
            ClickEvent.Action.SUGGEST_COMMAND,
            command);
    }

    public TextComponent commandComponent(Object sender, Text message, String command) {
        return commandComponent(sender,
            message,
            BukkitTranslationNode.SETUP_PROPERTY_SET_HOVER.formalText(),
            command);
    }

    @Command(names = "id")
    public void setId(CommandSender sender,
                      String id,
                      String newId) {
        Map<String, MapSession> sessionMap = bootstrap.getSessionMap();
        MapSession session = sessionMap.get(id);
        if (session != null) {
            session.setId(id);
            sessionMap.remove(id);
            sessionMap.put(newId, session);
            textHandler.send(sender,
                BukkitTranslationNode
                    .SESSION_ID_SET
                    .withFormal("{old}", id,
                        "{new}", newId));
            return;
        }

        Map<String, MapSerializedSession> serializedSessionMap = bootstrap.getSerializedSessionMap();
        MapSerializedSession serializedSession = serializedSessionMap.get(id);
        if (serializedSession != null) {
            serializedSession.setId(id);
            serializedSessionMap.remove(id);
            serializedSessionMap.put(newId, serializedSession);
            textHandler.send(sender,
                BukkitTranslationNode
                    .SESSION_ID_SET
                    .withFormal("{old}", id,
                        "{new}", newId));
            return;
        }

        textHandler.send(sender,
            TranslationNode
                .SESSION_OR_SERIALIZED_NOT_FOUND
                .withFormal("{id}", id));
    }

    @Command(names = "warning")
    public void switchWarning(CommandSender sender,
                              MapSession session,
                              boolean warning) {
        session.setWarning(warning);
        textHandler.send(sender,
            BukkitTranslationNode.SESSION_WARNING_SET.formalText(),
            session
        );
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
        ToolHandler toolHandler = plugin.getToolHandler();
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

        for (MapSession session : sessions) {
            textHandler.send(sender,
                BukkitTranslationNode
                    .SESSION_LIST_ENTRY
                    .text(),
                session);
        }
    }
}
