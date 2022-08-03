package team.unnamed.mappa.bukkit.command;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.ErrorHandler;
import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import me.fixeddev.commandflow.part.CommandPart;
import net.kyori.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
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
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.bukkit.util.Texts;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.event.EventBus;
import team.unnamed.mappa.internal.event.MappaSavedEvent;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.*;
import java.util.function.Function;

@Command(
    names = {"mappa", "map"},
    permission = "mappa.command"
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

    @Command(names = "verify",
        permission = "mappa.session.setup")
    public void verify(CommandSender sender,
                       @Switch("all") boolean showAll,
                       @Sender MapEditSession session) {
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

    @Command(names = "resume-sessions",
        permission = "mappa.session.control")
    public void resume(CommandContext context,
                       CommandSender sender) throws Throwable {
        try {
            bootstrap.resumeSessions(sender, true);
        } catch (Exception e) {
            errorHandler.handleException(context, e);
        }
    }

    @Command(names = {"resolve"},
        permission = "mappa.session.control")
    public void resolve(CommandContext context,
                        CommandSender sender,
                        MapSerializedSession serializedSession) throws Throwable {
        try {
            bootstrap.resumeSession(sender, serializedSession);
        } catch (Exception e) {
            errorHandler.handleException(context, e);
        }
    }

    @Command(names = "load",
        permission = "mappa.session.control")
    public void loadSessions(CommandSender sender,
                             MapScheme scheme) throws ParseException {
        bootstrap.loadSessions(scheme, sender);
    }

    @Command(names = {"new-session", "new"},
        permission = "mappa.session.control")
    public void newSession(CommandSender sender,
                           MapScheme scheme,
                           @OptArg("") String id) {
        MapEditSession session = id.isEmpty()
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

        Player player = (Player) sender;
        select(player, session);
        setupProperty(player, session, null);
    }

    @Command(names = "select")
    public void select(@Sender Player player, MapEditSession session) {
        Map<UUID, MapSession> entitySession = bootstrap.getEntitySession();
        entitySession.put(player.getUniqueId(), session);
        textHandler.send(player,
            TranslationNode
                .SELECTED_SESSION
                .formalText(),
            session);
    }

    @Command(names = "deselect")
    public void deselect(@Sender Player player,
                         @Sender MapEditSession session) {
        Map<UUID, MapSession> entitySession = bootstrap.getEntitySession();
        entitySession.remove(player.getUniqueId());
        textHandler.send(player,
            TranslationNode
                .DESELECTED_SESSION
                .formalText(),
            session);
    }

    @Command(names = {"skip-setup", "skip"},
        permission = "mappa.session.setup")
    public void skipSetupProperty(@Sender Player sender,
                                  @Sender MapEditSession session) {
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

    @Command(names = "show-setup",
        permission = "mappa.session.setup")
    public void showSetup(@Sender Player sender,
                          @Sender MapEditSession session) {
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

        Text header = BukkitTranslationNode
            .SETUP_HEADER
            .with(
                "{session_id}", sessionId
            );
        textHandler.send(sender, header);
        Text defineText = BukkitTranslationNode
            .DEFINE_PROPERTY
            .with("{property}", setupStep);
        MapProperty property = session.getProperty(setupStep);
        Text typeText = BukkitTranslationNode
            .TYPE_PROPERTY
            .with("{type}", Texts.getTypeName(property.getType()));

        CommandSchemeNodeBuilder builder = bootstrap.getCommandBuilder();
        PartInjector injector = builder.getInjector();
        CommandPart part = Commands.ofPart(injector, property.getType());
        Component component = part.getLineRepresentation();
        String arg = component == null
            ? ""
            : Texts.toString(component);

        Text lineText = BukkitTranslationNode
            .SETUP_PROPERTY_SET
            .with("{arg}", arg);
        Text optionalText = property.isOptional()
            ? BukkitTranslationNode.PROPERTY_SKIP_SETUP.text()
            : null;

        Player.Spigot spigot = sender.spigot();
        spigot.sendMessage(commandComponent(
            sender,
            defineText,
            BukkitTranslationNode.VIEW_PROPERTY_SET_HOVER.formalText(),
            ClickEvent.Action.RUN_COMMAND,
            line + " -v "));

        textHandler.send(sender, typeText);

        spigot.sendMessage(commandComponent(
            sender, lineText, "mappa setup "));

        if (optionalText != null) {
            sender.sendMessage(" ");
            spigot.sendMessage(commandComponent(
                sender, optionalText, "mappa skip-setup "));
        }

        Map<String, Text> errors = session.checkWithScheme(true);
        if (errors.isEmpty()) {
            textHandler.send(sender, BukkitTranslationNode.SETUP_READY.text());
        }
        textHandler.send(sender, header);
    }

    @Command(names = "setup",
        permission = "mappa.session.setup")
    public void setupProperty(@Sender Player sender,
                              @Sender MapEditSession session,
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
            showSetup(sender, session);
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

    @Command(names = "set-id",
        permission = "mappa.session.control")
    public void setId(CommandSender sender,
                      MapSession session,
                      String newId) throws ParseException {
        Map<String, MapSession> sessionMap = bootstrap.getSessionMap();
        if (sessionMap.containsKey(newId)) {
            textHandler.send(sender,
                BukkitTranslationNode
                    .SESSION_ALREADY_EXISTS
                    .withFormal("{id}", newId));
            return;
        }

        String oldId = session.getId();
        session.setId(newId);
        sessionMap.remove(oldId);
        sessionMap.put(newId, session);
        textHandler.send(sender,
            BukkitTranslationNode
                .SESSION_ID_SET
                .withFormal("{old}", oldId,
                    "{new}", newId));
        MapScheme scheme = session.getScheme();
        String path = scheme.getObject(MapScheme.SESSION_ID_PATH);
        if (path == null) {
            return;
        }

        session.property(path, newId);
        textHandler.send(sender,
            TranslationNode
                .PROPERTY_CHANGE_TO
                .withFormal("{name}", path,
                    "{value}", newId));
    }

    @Command(names = "set-warning",
        permission = "mappa.session.control")
    public void switchWarning(CommandSender sender,
                              MapSession session,
                              boolean warning) {
        session.setWarning(warning);
        textHandler.send(sender,
            BukkitTranslationNode.SESSION_WARNING_SET.formalText(),
            session
        );
    }

    @Command(names = {"vector-tool", "vector"},
        permission = "mappa.tool.vector-tool")
    public void newVectorTool(@Sender Player player) {
        createTool(player,
            ToolHandler.VECTOR_TOOL,
            Material.STICK,
            BukkitTranslationNode.TOOL_VECTOR_NAME);
    }

    @Command(names = {"centered-vector-tool", "centered-vector"},
        permission = "mappa.tool.centered-vector-tool")
    public void newCenteredVectorTool(@Sender Player player) {
        createTool(player,
            ToolHandler.CENTERED_VECTOR_TOOL,
            Material.WOOD_SPADE,
            BukkitTranslationNode.TOOL_CENTERED_VECTOR_NAME);
    }

    @Command(names = {"precise-vector-tool", "precise-vector"},
        permission = "mappa.tool.precise-vector-tool")
    public void newPreciseVectorTool(@Sender Player player) {
        createTool(player,
            ToolHandler.PRECISE_VECTOR_TOOL,
            Material.ARROW,
            BukkitTranslationNode.TOOL_PRECISE_VECTOR_NAME);
    }

    @Command(names = {"yaw-pitch-tool", "yaw-pitch"},
        permission = "mappa.tool.yaw-pitch-tool")
    public void newYawPitchTool(@Sender Player player) {
        createTool(player,
            ToolHandler.YAW_PITCH_TOOL,
            Material.TRIPWIRE_HOOK,
            BukkitTranslationNode.TOOL_YAW_PITCH_NAME);
    }

    @Command(names = {"centered-yaw-pitch-tool", "centered-yaw-pitch", "cyp"},
        permission = "mappa.tool.centered-yaw-pitch-tool")
    public void newCenteredYawPitchTool(@Sender Player player) {
        createTool(player,
            ToolHandler.CENTERED_YAW_PITCH_TOOL,
            Material.LEVER,
            BukkitTranslationNode.TOOL_CENTERED_YAW_PITCH_NAME);
    }

    @Command(names = {"mirror-vector-tool", "mirror-vector"},
        permission = "mappa.tool.mirror-vector-tool")
    public void newMirrorVectorTool(@Sender Player player) {
        createTool(player,
            ToolHandler.MIRROR_VECTOR_TOOL,
            Material.STAINED_GLASS_PANE,
            BukkitTranslationNode.TOOL_MIRROR_VECTOR_NAME);
    }

    @Command(names = {"region-radius-tool", "region-radius"},
        permission = "mappa.tool.region-radius-tool")
    public void newRegionRadiusTool(@Sender Player player,
                                    int radius) {
        createTool(player,
            ToolHandler.REGION_RADIUS_TOOL,
            Material.WEB,
            BukkitTranslationNode.TOOL_REGION_RADIUS,
            itemStack -> {
                itemStack = NBTEditor.set(itemStack, radius, ToolHandler.REGION_RADIUS);
                List<String> lore = new ArrayList<>();
                String value = textHandler.format(player, BukkitTranslationNode
                    .TOOL_REGION_RADIUS_LORE
                    .with("{radius}", radius));
                lore.add(value);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            });
    }

    @Command(names = {"custom-region-radius-tool", "custom-region-radius"},
        permission = "mappa.tool.custom-region-radius-tool")
    public void newCustomRegionRadiusTool(@Sender Player player,
                                          int x,
                                          int yPlus,
                                          int yMinus,
                                          int z) {
        createTool(player,
            ToolHandler.CUSTOM_REGION_RADIUS_TOOL,
            Material.WEB,
            BukkitTranslationNode.TOOL_CUSTOM_REGION_RADIUS,
            itemStack -> {
                itemStack = NBTEditor.set(itemStack, x, ToolHandler.REGION_X_RADIUS);
                itemStack = NBTEditor.set(itemStack, yPlus, ToolHandler.REGION_Y_PLUS_RADIUS);
                itemStack = NBTEditor.set(itemStack, yMinus, ToolHandler.REGION_Y_MINUS_RADIUS);
                itemStack = NBTEditor.set(itemStack, z, ToolHandler.REGION_Z_RADIUS);

                List<String> lore = new ArrayList<>();
                String xValue = textHandler.format(player, BukkitTranslationNode
                    .TOOL_CUSTOM_REGION_RADIUS_X
                    .with("{radius}", x));
                String yPlusValue = textHandler.format(player, BukkitTranslationNode
                    .TOOL_CUSTOM_REGION_RADIUS_Y_PLUS
                    .with("{radius}", yPlus));
                String yMinusValue = textHandler.format(player, BukkitTranslationNode
                    .TOOL_CUSTOM_REGION_RADIUS_Y_MINUS
                    .with("{radius}", yMinus));
                String zValue = textHandler.format(player, BukkitTranslationNode
                    .TOOL_CUSTOM_REGION_RADIUS_Z
                    .with("{radius}", z));
                Collections.addAll(lore,
                    xValue,
                    yPlusValue,
                    yMinusValue,
                    zValue);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setLore(lore);
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
        );
    }

    @Command(names = {"chunk-tool", "chunk"},
        permission = "mappa.tool.chunk-tool")
    public void newChunkTool(@Sender Player player) {
        createTool(player,
            ToolHandler.CHUNK_TOOL,
            Material.BLAZE_ROD,
            BukkitTranslationNode.TOOL_CHUNK_NAME);
    }

    @Command(names = {"tool"},
        permission = "mappa.tool.custom")
    public void newTool(@Sender Player player,
                        String toolId) {
        createTool(player,
            toolId,
            Material.GOLD_HOE,
            BukkitTranslationNode.TOOL_CUSTOM_NAME);
    }

    @Command(names = {"basic-tools"},
        permission = "mappa.tool.basic-tools")
    public void getBasicTools(@Sender Player player) {
        newVectorTool(player);
        newCenteredVectorTool(player);
        newPreciseVectorTool(player);
        newYawPitchTool(player);
        newCenteredYawPitchTool(player);
        newChunkTool(player);
    }

    public void createTool(Player player,
                           String toolId,
                           Material material,
                           BukkitTranslationNode node) {
        createTool(player, toolId, material, node, null);
    }

    public void createTool(Player player,
                           String toolId,
                           Material material,
                           BukkitTranslationNode node,
                           Function<ItemStack, ItemStack> function) {
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
        if (function != null) {
            itemStack = function.apply(itemStack);
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        Text textNode = node.withFormal("{id}", toolId);
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

    @Command(names = {"scanner-tool", "scanner-vector-tool"},
        permission = "mappa.tool.scanner-vector-tool")
    public void createScannerTool(@Sender Player player,
                                  MapScheme scheme,
                                  String path,
                                  int radius,
                                  @Switch("delete-block") boolean deleteBlock,
                                  @Switch("delete-marker") boolean deleteMarker) {
        ToolHandler toolHandler = plugin.getToolHandler();
        String toolId = ToolHandler.SCANNER_VECTOR_TOOL;
        Tool<Player> tool = toolHandler.getToolById(toolId, player);
        if (tool == null) {
            textHandler.send(player,
                BukkitTranslationNode
                    .TOOL_NOT_FOUND
                    .withFormal("{id}", toolId));
            return;
        }

        ItemStack itemStack = new ItemStack(Material.REDSTONE_TORCH_ON);
        itemStack = NBTEditor.set(itemStack, toolId, SelectionListener.TOOL_ID);
        String schemeName = scheme.getName();
        itemStack = NBTEditor.set(itemStack, schemeName, ToolHandler.SCAN_SCHEME);
        itemStack = NBTEditor.set(itemStack, path, ToolHandler.SCAN_PATH);
        itemStack = NBTEditor.set(itemStack, radius, ToolHandler.SCAN_RADIUS);
        if (deleteBlock) {
            itemStack = NBTEditor.set(itemStack, true, ToolHandler.SCAN_DELETE_BLOCK);
        }
        if (deleteMarker) {
            itemStack = NBTEditor.set(itemStack, true, ToolHandler.SCAN_DELETE_MARKER);
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        Text textNode = BukkitTranslationNode
            .TOOL_SCANNER_VECTOR_NAME
            .text();
        itemMeta.setDisplayName(
            textHandler.format(player, textNode));

        List<String> lore = new ArrayList<>();
        String firstLine = textHandler.format(player,
            BukkitTranslationNode
                .TOOL_SCANNER_VECTOR_LORE_SCHEME
                .withFormal("{scheme}", schemeName));
        String secondLine = textHandler.format(player,
            BukkitTranslationNode
                .TOOL_SCANNER_VECTOR_LORE_PATH
                .withFormal("{path}", path));
        String thirdLine = textHandler.format(player,
            BukkitTranslationNode
                .TOOL_SCANNER_VECTOR_LORE_RADIUS
                .withFormal("{radius}", radius));
        Collections.addAll(lore,
            firstLine, secondLine, thirdLine);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        PlayerInventory inventory = player.getInventory();
        inventory.addItem(itemStack);

        textHandler.send(player,
            BukkitTranslationNode
                .TOOL_RECEIVED
                .withFormal("{id}", toolId));
    }

    @Command(names = {"vector-pos-1", "vpos1"})
    public void setFirstVector(@Sender Player player,
                               @Switch("looking-at") boolean lookingAt) {
        setVectorSelection(player, true, lookingAt);
    }

    @Command(names = {"vector-pos-2", "vpos2"})
    public void setSecondVector(@Sender Player player,
                                @Switch("looking-at") boolean lookingAt) {
        setVectorSelection(player, false, lookingAt);
    }

    public void setVectorSelection(Player player, boolean first, boolean lookingAt) {
        RegionRegistry regionRegistry = plugin.getRegionRegistry();
        String uuid = player.getUniqueId().toString();
        RegionSelection<Vector> selection =
            regionRegistry.getOrNewVectorSelection(uuid);

        Vector newVector;
        if (lookingAt) {
            Block targetBlock = player.getTargetBlock((Set<Material>) null, 5);
            if (targetBlock.isEmpty()) {
                BukkitTranslationNode node = first
                    ? BukkitTranslationNode.NO_FIRST_SELECTION
                    : BukkitTranslationNode.NO_SECOND_SELECTION;
                textHandler.send(player,
                    node.withFormal(
                        "{type}", Texts.getTypeName(Vector.class)));
                return;
            }
            newVector = MappaBukkit.toMappaVector(targetBlock);
        } else {
            Location location = player.getLocation();
            Block block = location.getBlock();
            // To block location without decimals
            location = block.getLocation();
            newVector = MappaBukkit.toMappa(location);
        }

        String typeName = Texts.getTypeName(Vector.class);
        BukkitTranslationNode node;
        if (first) {
            selection.setFirstPoint(newVector);
            node = BukkitTranslationNode.FIRST_POINT_SELECTED;
        } else {
            selection.setSecondPoint(newVector);
            node = BukkitTranslationNode.SECOND_POINT_SELECTED;
        }

        textHandler.send(player,
            node
                .with("{type}", typeName,
                    "{location}", Vector.toString(newVector)));
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

    @Command(names = {"list", "ls", "sessions"},
        permission = "mappa.session.list")
    public void showSessions(CommandSender sender) {
        Collection<MapSession> sessions = bootstrap.getSessions();
        int size = sessions.size();
        if (size == 0) {
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
                    "{number}", size
                ));

        for (MapSession session : sessions) {
            String line;
            if (session instanceof MapSerializedSession) {
                MapSerializedSession serializedSession = (MapSerializedSession) session;
                ChatColor color = getColorOfReason(serializedSession.getReason());
                String schemeName = serializedSession.getSchemeName();
                ChatColor schemeColor = bootstrap.getScheme(schemeName) == null
                    ? ChatColor.RED
                    : ChatColor.GOLD;
                line = textHandler.format(sender,
                    BukkitTranslationNode
                        .SESSION_LIST_ENTRY
                        .with("{session_id}", color + serializedSession.getId(),
                            "{session_scheme}", schemeColor + schemeName));
            } else {
                line = textHandler.format(sender,
                    BukkitTranslationNode
                        .SESSION_LIST_ENTRY
                        .text(),
                    session);
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(line);
                return;
            }

            Player player = (Player) sender;
            TextComponent component = new TextComponent(line);
            String id = session.getId();
            if (session instanceof MapEditSession) {
                ClickEvent clickEvent = new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, "/mappa select " + id);
                component.setClickEvent(clickEvent);

                String hover = textHandler.format(sender,
                    BukkitTranslationNode
                        .SESSION_LIST_ENTRY_SELECT
                        .with("{id}", id));
                TextComponent hoverComponent = new TextComponent(hover);
                HoverEvent hoverEvent = new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hoverComponent});
                component.setHoverEvent(hoverEvent);
            } else if (session instanceof MapSerializedSession) {
                ClickEvent clickEvent = new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, "/mappa resolve " + id);
                component.setClickEvent(clickEvent);

                String resolve = textHandler.format(sender,
                    BukkitTranslationNode
                        .SESSION_LIST_ENTRY_RESOLVE
                        .with("{id}", id));
                String reason = textHandler.format(sender,
                    BukkitTranslationNode
                        .SESSION_LIST_ENTRY_REASON
                        .text(),
                    session);
                BaseComponent[] components = {
                    new TextComponent(resolve),
                    new TextComponent("\n"),
                    new TextComponent(reason)
                };
                HoverEvent hoverEvent = new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    components);
                component.setHoverEvent(hoverEvent);
            }

            player.spigot().sendMessage(component);
        }
    }

    private ChatColor getColorOfReason(MapSerializedSession.Reason reason) {
        return ChatColor.valueOf(reason.getColorName());
    }

    @Command(names = {"save-all"},
        permission = "mappa.session.control")
    public void saveAllSession(CommandContext context,
                               CommandSender sender,
                               @OptArg("false") boolean clearSessions) throws Throwable {
        FileConfiguration config = plugin.getConfig();
        try {
            bootstrap.saveAll(sender,
                config.getBoolean("unload.save-ready-sessions"),
                clearSessions);
        } catch (Exception e) {
            errorHandler.handleException(context, e);
        }
    }

    @Command(names = {"save"},
        permission = "mappa.session.control")
    public void saveSession(CommandSender sender, MapEditSession session) {
        bootstrap.markToSave(sender, session.getId());
        EventBus eventBus = bootstrap.getEventBus();
        eventBus.callEvent(new MappaSavedEvent(sender, session));
    }

    @Command(names = {"delete-session", "remove-session"},
        permission = "mappa.session.control")
    public void deleteSession(CommandSender sender, MapSession session) {
        bootstrap.removeSession(sender, session);
    }
}
