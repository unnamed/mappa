package team.unnamed.mappa.bukkit.tool;

import com.cryptomorin.xseries.XSound;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.Texts;

public class RegionRadiusTool extends AbstractBukkitTool {

    protected RegionRadiusTool(String id, RegionRegistry regionRegistry, MappaTextHandler textHandler) {
        super(id,
            false,
            regionRegistry,
            textHandler,
            Vector.class);
    }


    public RegionRadiusTool(RegionRegistry regionRegistry, MappaTextHandler textHandler) {
        this(ToolHandler.REGION_RADIUS_TOOL, regionRegistry, textHandler);
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        ItemStack itemInHand = entity.getItemInHand();
        int radius = NBTEditor.getInt(itemInHand, ToolHandler.REGION_RADIUS);
        if (!checkNonNegative(radius)) {
            textHandler.send(entity, BukkitTranslationNode.RADIUS_AXIS_NON_NEGATIVE);
            return;
        }

        Cuboid expanded = lookingAt.expand(radius, radius, radius);
        setByRadius(entity, expanded);
    }

    protected void setByRadius(Player entity,
                               Cuboid expanded) {
        String uniqueId = entity.getUniqueId().toString();
        RegionSelection<Vector> selection = regionRegistry
            .getOrNewVectorSelection(uniqueId);

        Vector maximum = expanded.getMaximum();
        Vector minimum = expanded.getMinimum();
        selection.setFirstPoint(maximum);
        selection.setSecondPoint(minimum);

        String typeName = Texts.getTypeName(Vector.class);
        textHandler.send(entity,
            BukkitTranslationNode
                .FIRST_POINT_SELECTED
                .with("{type}", typeName,
                    "{location}", Vector.toString(maximum)));
        textHandler.send(entity,
            BukkitTranslationNode
                .SECOND_POINT_SELECTED
                .with("{type}", typeName,
                    "{location}", Vector.toString(minimum)));
        XSound.BLOCK_NOTE_BLOCK_PLING.play(entity, 1.0F, 0.5F);
        XSound.BLOCK_NOTE_BLOCK_PLING.play(entity, 1.0F, 1.0F);
    }

    protected boolean checkNonNegative(int... ints) {
        for (int anInt : ints) {
            if (anInt < 0) {
                return false;
            }
        }
        return true;
    }
}
