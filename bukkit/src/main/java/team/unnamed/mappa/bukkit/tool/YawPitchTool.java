package team.unnamed.mappa.bukkit.tool;

import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.util.MathUtils;
import team.unnamed.mappa.bukkit.util.Texts;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.Vector;

public class YawPitchTool extends AbstractBukkitTool {

    public YawPitchTool(String id,
                        boolean interactAir,
                        RegionRegistry regionRegistry,
                        MappaTextHandler textHandler) {
        super(id,
            interactAir,
            regionRegistry,
            textHandler);
    }

    public YawPitchTool(RegionRegistry regionRegistry, MappaTextHandler textHandler) {
        super(ToolHandler.YAW_PITCH_TOOL,
            true,
            regionRegistry,
            textHandler);
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        String uniqueId = entity.getUniqueId().toString();
        RegionSelection<Vector> vectorSelection =
            regionRegistry.getOrNewVectorSelection(uniqueId);

        BukkitTranslationNode text;
        Vector point;
        String typeName = Texts.getTypeName(Vector.class);
        float soundPitch;
        if (button == Tool.Button.RIGHT) {
            point = vectorSelection.getFirstPoint();
            if (point == null) {
                textHandler.send(entity,
                    BukkitTranslationNode
                        .FIRST_POINT_NOT_EXISTS
                        .with("{type}", typeName));
                return;
            }

            // Where is my boilerplate?!!
            double yaw = MathUtils.fixYaw(lookingAt.getYaw());
            double pitch = lookingAt.getPitch();
            point = point.mutYawPitch(yaw, pitch);
            vectorSelection.setFirstPoint(point);
            text = BukkitTranslationNode.FIRST_YAW_PITCH_SELECTED;
            soundPitch = 0.5F;
        } else {
            point = vectorSelection.getSecondPoint();
            if (point == null) {
                textHandler.send(entity,
                    BukkitTranslationNode
                        .SECOND_POINT_NOT_EXISTS
                        .with("{type}", typeName));
                return;
            }

            double yaw = MathUtils.fixYaw(lookingAt.getYaw());
            double pitch = lookingAt.getPitch();
            point = point.mutYawPitch(yaw, pitch);
            vectorSelection.setSecondPoint(point);
            text = BukkitTranslationNode.SECOND_YAW_PITCH_SELECTED;
            soundPitch = 1.0F;
        }

        Text node = text.with("{location}", point.getYaw() + ", " + point.getPitch());
        textHandler.send(entity, node);
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(entity, 1.0F, soundPitch);
    }
}
