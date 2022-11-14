package team.unnamed.mappa.bukkit.tool;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.Vector;

public class CustomRegionRadiusTool extends RegionRadiusTool {

    public CustomRegionRadiusTool(RegionRegistry regionRegistry,
                                  MappaTextHandler textHandler) {
        super(ToolHandler.CUSTOM_REGION_RADIUS_TOOL, regionRegistry, textHandler);
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        ItemStack itemInHand = entity.getItemInHand();
        int x = NBTEditor.getInt(itemInHand, ToolHandler.REGION_X_RADIUS);
        int yPlus = NBTEditor.getInt(itemInHand, ToolHandler.REGION_Y_PLUS_RADIUS);
        int yMinus = NBTEditor.getInt(itemInHand, ToolHandler.REGION_Y_MINUS_RADIUS);
        int z = NBTEditor.getInt(itemInHand, ToolHandler.REGION_Z_RADIUS);
        if (!checkNonNegative(x, yPlus, yMinus, z)) {
            textHandler.send(entity, BukkitTranslationNode.RADIUS_AXIS_NON_NEGATIVE);
            return;
        }

        Cuboid expanded = lookingAt.expand(x, yPlus, yMinus, z);
        super.setByRadius(entity, expanded);
    }
}
