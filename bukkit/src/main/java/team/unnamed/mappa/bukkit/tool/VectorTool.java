package team.unnamed.mappa.bukkit.tool;

import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.util.Texts;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.Vector;

public class VectorTool extends AbstractBukkitTool {

    public VectorTool(String id,
                      boolean interactAir,
                      RegionRegistry regionRegistry,
                      MappaTextHandler textHandler) {
        super(id,
            interactAir,
            regionRegistry,
            textHandler);
    }

    public VectorTool(RegionRegistry regionRegistry,
                      MappaTextHandler textHandler) {
        this(ToolHandler.VECTOR_TOOL,
            false,
            regionRegistry,
            textHandler);
    }

    @Override
    public void interact(Player entity,
                         Vector lookingAt,
                         Button button,
                         boolean shift) {
        String uniqueId = entity.getUniqueId().toString();
        RegionSelection<Vector> vectorSelection =
            regionRegistry.getOrNewVectorSelection(uniqueId);

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
        } else {
            vectorSelection.setSecondPoint(lookingAt);
            text = shift
                ? BukkitTranslationNode.SECOND_POINT_FLOOR_SELECTED
                : BukkitTranslationNode.SECOND_POINT_SELECTED;
            soundPitch = 1.0F;
        }
        TextNode node = text.with(
            "{type}", Texts.getTypeName(Vector.class),
            "{location}", Vector.toString(lookingAt));
        textHandler.send(entity, node);
        XSound.UI_BUTTON_CLICK.play(entity, 1.0F, soundPitch);
    }
}
