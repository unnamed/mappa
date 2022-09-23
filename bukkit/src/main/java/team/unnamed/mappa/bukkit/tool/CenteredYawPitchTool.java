package team.unnamed.mappa.bukkit.tool;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.MathUtils;

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
        Location location = entity.getLocation();
        super.interact(entity,
            MathUtils.roundAllDecimals(location.getYaw()),
            MathUtils.roundAllDecimals(location.getPitch()),
            button);
    }
}
