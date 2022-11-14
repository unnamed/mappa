package team.unnamed.mappa.bukkit.tool;

import org.bukkit.entity.Player;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.tool.AbstractTool;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.object.Vector;

public abstract class AbstractBukkitTool extends AbstractTool {
    protected final RegionRegistry regionRegistry;
    protected final MappaTextHandler textHandler;

    public AbstractBukkitTool(String id,
                              boolean interactAir,
                              RegionRegistry regionRegistry,
                              MappaTextHandler textHandler,
                              Class<?> selectionType) {
        super(id, "mappa.tool", interactAir, selectionType);
        this.regionRegistry = regionRegistry;
        this.textHandler = textHandler;
    }

    @Override
    public void interact(MappaPlayer entity, Vector lookingAt, Button button, boolean shift) {
        interact((Player) entity.asEntity(), lookingAt, button, shift);
    }

    protected void interact(Player entity, Vector lookingAt, Button button, boolean shift) {}
}
