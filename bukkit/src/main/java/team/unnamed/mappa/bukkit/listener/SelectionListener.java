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
import org.bukkit.inventory.ItemStack;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.bukkit.util.MathUtils;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.object.Vector;

import java.util.Map;
import java.util.function.Consumer;

public class SelectionListener implements Listener {
    public static final String TOOL_ID = "tool-id";

    private final ToolHandler handler;
    private final Map<Integer, Consumer<Projectile>> projectiles;

    public SelectionListener(ToolHandler handler, Map<Integer, Consumer<Projectile>> projectiles) {
        this.handler = handler;
        this.projectiles = projectiles;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        Consumer<Projectile> runnable = projectiles.get(entity.getEntityId());
        if (runnable == null) {
            return;
        }

        runnable.accept(entity);
        entity.remove();
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
    }
}
