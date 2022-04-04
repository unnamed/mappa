package team.unnamed.mappa.bukkit.listener;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;

public class SelectionListener implements Listener {
    public static final String TOOL_ID = "tool-id";

    private final ToolHandler handler;

    public SelectionListener(ToolHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onToolHandle(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        ItemStack item = event.getItem();
        if (item == null || clickedBlock == null) {
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

        tool.interact(player, MappaBukkit.toMappaVector(clickedBlock), button);
        event.setCancelled(true);
    }
}
