package team.unnamed.mappa.bukkit.listener;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.bukkit.internal.BukkitVisualizer;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.bukkit.util.MathUtils;
import team.unnamed.mappa.internal.event.MappaRegionSelectEvent;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.model.visualizer.PropertyVisual;
import team.unnamed.mappa.model.visualizer.Visual;
import team.unnamed.mappa.object.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SelectionListener implements Listener {
    public static final String TOOL_ID = "tool-id";

    private final ToolHandler handler;
    private final RegionRegistry regionRegistry;
    private final BukkitVisualizer visualizer;
    private final EventBus eventBus;
    private final Map<Integer, Consumer<Projectile>> projectiles;
    private final Map<UUID, MapSession> entitySession;

    public SelectionListener(MappaPlugin api) {
        this.handler = api.getToolHandler();
        this.regionRegistry = api.getRegionRegistry();
        this.visualizer = api.getVisualizer();
        this.eventBus = api.getBootstrap().getEventBus();
        this.projectiles = api.getProjectileCache();
        this.entitySession = api.getBootstrap().getEntitySession();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        entitySession.remove(uuid);
        List<PropertyVisual<Player>> visuals = visualizer.getVisualsOf(player);
        if (visuals != null) {
            for (PropertyVisual<Player> visual : visuals) {
                visual.hide(player);
            }
        }

        Map<UUID, Visual> selectionVisuals = visualizer.getSelectionVisual();
        Visual selectionVisual = selectionVisuals.get(uuid);
        if (selectionVisual != null) {
            selectionVisuals.remove(uuid);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        int entityId = entity.getEntityId();
        Consumer<Projectile> runnable = projectiles.get(entityId);
        if (runnable == null) {
            return;
        }

        runnable.accept(entity);
        entity.remove();
        projectiles.remove(entityId);
    }

    @EventHandler
    public void onToolHandle(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        String id = NBTEditor.getString(item, TOOL_ID);
        if (id == null) {
            return;
        }

        Player player = event.getPlayer();
        Tool<Player> tool = handler.getToolById(id, player);
        if (tool == null || !player.hasPermission(tool.getPermission())) {
            return;
        }

        Action action = event.getAction();
        Tool.Button button = MappaBukkit.toMappa(action);
        if (button == null) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Vector lookingAt;
        if (clickedBlock == null) {
            if (!tool.canInteractWithAir()) {
                return;
            }

            lookingAt = MappaBukkit.toMappa(player.getLocation().toVector());
            lookingAt = MathUtils.roundVector(lookingAt);
        } else {
            lookingAt = MappaBukkit.toMappaVector(clickedBlock);
        }

        tool.interact(player, lookingAt, button, player.isSneaking());
        event.setCancelled(true);

        Class<?> selectionType = tool.getSelectionType();
        if (selectionType == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        RegionSelection<?> selection = regionRegistry.getSelection(
            uuid.toString(), selectionType);
        if (selection == null) {
            return;
        }
        eventBus.callEvent(new MappaRegionSelectEvent(player, selection));
    }
}
