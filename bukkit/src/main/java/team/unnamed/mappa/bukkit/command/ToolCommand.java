package team.unnamed.mappa.bukkit.command;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.bukkit.listener.SelectionListener;
import team.unnamed.mappa.command.HelpCommand;
import team.unnamed.mappa.internal.command.parts.Path;
import team.unnamed.mappa.internal.command.parts.Sender;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Command(names = "tool")
public class ToolCommand extends HelpCommand {
    private final MappaPlugin plugin;

    public ToolCommand(MappaAPI plugin) {
        this.plugin = (MappaPlugin) plugin;
    }

    @Command(names = {"help", "?"})
    public void onHelp(MappaPlayer sender, @OptArg("1") int page, CommandContext context) {
        help(sender, page, context);
    }

    @Command(names = {"vector-tool", "vector"},
        permission = "mappa.tool.vector-tool")
    public void newVectorTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.VECTOR_TOOL,
            Material.STICK,
            BukkitTranslationNode.TOOL_VECTOR_NAME);
    }

    @Command(names = {"centered-vector-tool", "centered-vector"},
        permission = "mappa.tool.centered-vector-tool")
    public void newCenteredVectorTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.CENTERED_VECTOR_TOOL,
            Material.WOOD_SPADE,
            BukkitTranslationNode.TOOL_CENTERED_VECTOR_NAME);
    }

    @Command(names = {"precise-vector-tool", "precise-vector"},
        permission = "mappa.tool.precise-vector-tool")
    public void newPreciseVectorTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.PRECISE_VECTOR_TOOL,
            Material.ARROW,
            BukkitTranslationNode.TOOL_PRECISE_VECTOR_NAME);
    }

    @Command(names = {"yaw-pitch-tool", "yaw-pitch"},
        permission = "mappa.tool.yaw-pitch-tool")
    public void newYawPitchTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.YAW_PITCH_TOOL,
            Material.TRIPWIRE_HOOK,
            BukkitTranslationNode.TOOL_YAW_PITCH_NAME);
    }

    @Command(names = {"centered-yaw-pitch-tool", "centered-yaw-pitch", "cyp"},
        permission = "mappa.tool.centered-yaw-pitch-tool")
    public void newCenteredYawPitchTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.CENTERED_YAW_PITCH_TOOL,
            Material.LEVER,
            BukkitTranslationNode.TOOL_CENTERED_YAW_PITCH_NAME);
    }

    @Command(names = {"mirror-vector-tool", "mirror-vector"},
        permission = "mappa.tool.mirror-vector-tool")
    public void newMirrorVectorTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.MIRROR_VECTOR_TOOL,
            Material.STAINED_GLASS_PANE,
            BukkitTranslationNode.TOOL_MIRROR_VECTOR_NAME);
    }

    @Command(names = {"region-radius-tool", "region-radius"},
        permission = "mappa.tool.region-radius-tool")
    public void newRegionRadiusTool(@Sender MappaPlayer sender,
                                    int radius) {
        createTool(sender,
            ToolHandler.REGION_RADIUS_TOOL,
            Material.WEB,
            BukkitTranslationNode.TOOL_REGION_RADIUS,
            itemStack -> {
                itemStack = NBTEditor.set(itemStack, radius, ToolHandler.REGION_RADIUS);
                List<String> lore = new ArrayList<>();
                String value = sender.format(BukkitTranslationNode
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
    public void newCustomRegionRadiusTool(@Sender MappaPlayer player,
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
                String xValue = player.format(BukkitTranslationNode
                    .TOOL_CUSTOM_REGION_RADIUS_X
                    .with("{radius}", x));
                String yPlusValue = player.format(BukkitTranslationNode
                    .TOOL_CUSTOM_REGION_RADIUS_Y_PLUS
                    .with("{radius}", yPlus));
                String yMinusValue = player.format(BukkitTranslationNode
                    .TOOL_CUSTOM_REGION_RADIUS_Y_MINUS
                    .with("{radius}", yMinus));
                String zValue = player.format(BukkitTranslationNode
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

    @Command(names = {"armor-stand-tool", "armor-stand"},
        permission = "mappa.tool.armor-stand-tool")
    public void newArmorStandTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.ARMOR_STAND_TOOL,
            Material.ARMOR_STAND,
            BukkitTranslationNode.TOOL_ARMOR_STAND_NAME);
    }

    @Command(names = {"chunk-tool", "chunk"},
        permission = "mappa.tool.chunk-tool")
    public void newChunkTool(@Sender MappaPlayer player) {
        createTool(player,
            ToolHandler.CHUNK_TOOL,
            Material.BLAZE_ROD,
            BukkitTranslationNode.TOOL_CHUNK_NAME);
    }

    @Command(names = {"tool"},
        permission = "mappa.tool.custom")
    public void newTool(@Sender MappaPlayer player,
                        String toolId) {
        createTool(player,
            toolId,
            Material.GOLD_HOE,
            BukkitTranslationNode.TOOL_CUSTOM_NAME);
    }

    @Command(names = {"basic-tools"},
        permission = "mappa.tool.basic-tools")
    public void getBasicTools(@Sender MappaPlayer player) {
        newVectorTool(player);
        newCenteredVectorTool(player);
        newPreciseVectorTool(player);
        newYawPitchTool(player);
        newCenteredYawPitchTool(player);
        newChunkTool(player);
    }

    public void createTool(MappaPlayer player,
                           String toolId,
                           Material material,
                           BukkitTranslationNode node) {
        createTool(player, toolId, material, node, null);
    }

    public void createTool(MappaPlayer player,
                           String toolId,
                           Material material,
                           BukkitTranslationNode node,
                           Function<ItemStack, ItemStack> function) {
        ToolHandler toolHandler = plugin.getToolHandler();
        Tool tool = toolHandler.getById(toolId);
        if (tool == null) {
            player.send(
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
            player.format(textNode));
        itemStack.setItemMeta(itemMeta);
        Player cast = player.cast();
        PlayerInventory inventory = cast.getInventory();
        inventory.addItem(itemStack);

        player.send(
            BukkitTranslationNode
                .TOOL_RECEIVED
                .withFormal("{id}", toolId));
    }

    @Command(names = {"scanner-tool", "scanner-vector-tool"},
        permission = "mappa.tool.scanner-vector-tool")
    public void createScannerTool(@Sender MappaPlayer player,
                                  MapScheme scheme,
                                  @Path String path,
                                  int radius,
                                  @Switch("delete-block") boolean deleteBlock,
                                  @Switch("delete-marker") boolean deleteMarker) {
        ToolHandler toolHandler = plugin.getToolHandler();
        String toolId = ToolHandler.SCANNER_VECTOR_TOOL;
        Tool tool = toolHandler.getById(toolId);
        if (tool == null) {
            player.send(
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
            player.format(textNode));

        List<String> lore = new ArrayList<>();
        String firstLine = player.format(
            BukkitTranslationNode
                .TOOL_SCANNER_VECTOR_LORE_SCHEME
                .withFormal("{scheme}", schemeName));
        String secondLine = player.format(
            BukkitTranslationNode
                .TOOL_SCANNER_VECTOR_LORE_PATH
                .withFormal("{path}", path));
        String thirdLine = player.format(
            BukkitTranslationNode
                .TOOL_SCANNER_VECTOR_LORE_RADIUS
                .withFormal("{radius}", radius));
        Collections.addAll(lore,
            firstLine, secondLine, thirdLine);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        Player bukkit = player.cast();
        PlayerInventory inventory = bukkit.getInventory();
        inventory.addItem(itemStack);

        player.send(
            BukkitTranslationNode
                .TOOL_RECEIVED
                .withFormal("{id}", toolId));
    }
}
