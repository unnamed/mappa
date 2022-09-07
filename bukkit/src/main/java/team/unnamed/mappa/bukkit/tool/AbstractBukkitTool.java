package team.unnamed.mappa.bukkit.tool;

import org.bukkit.entity.Player;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.tool.AbstractTool;

public abstract class AbstractBukkitTool extends AbstractTool<Player> {
    protected final RegionRegistry regionRegistry;
    protected final MappaTextHandler textHandler;

    public AbstractBukkitTool(String id,
                              boolean interactAir,
                              RegionRegistry regionRegistry,
                              MappaTextHandler textHandler,
                              Class<?> selectionType) {
        super(id, "mappa.tool", interactAir, Player.class, selectionType);
        this.regionRegistry = regionRegistry;
        this.textHandler = textHandler;
    }
}
