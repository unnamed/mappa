package team.unnamed.mappa.bukkit.tool;

import org.bukkit.entity.Player;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.object.Vector;

public class CenteredVectorTool extends VectorTool{

    public CenteredVectorTool(RegionRegistry regionRegistry, MappaTextHandler textHandler) {
        super(ToolHandler.CENTERED_VECTOR_TOOL,
            false,
            regionRegistry,
            textHandler);
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        super.interact(entity, lookingAt.sum(0.5, 0, 0.5), button, shift);
    }
}
