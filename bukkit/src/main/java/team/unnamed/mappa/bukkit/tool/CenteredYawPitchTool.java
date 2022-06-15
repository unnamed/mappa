package team.unnamed.mappa.bukkit.tool;

import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.bukkit.util.MathUtils;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.object.Vector;

public class CenteredYawPitchTool extends YawPitchTool {

    public CenteredYawPitchTool(RegionRegistry regionRegistry,
                                MappaTextHandler textHandler) {
        super(ToolHandler.CENTERED_YAW_PITCH_TOOL,
            true,
            regionRegistry,
            textHandler);
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        lookingAt = MappaBukkit.toMappa(entity.getLocation());
        double yaw = MathUtils.roundAllDecimals(lookingAt.getYaw());
        double pitch = MathUtils.roundAllDecimals(lookingAt.getPitch());
        lookingAt = lookingAt.mutYawPitch(yaw, pitch);
        super.interact(entity, lookingAt, button, shift);
    }
}
