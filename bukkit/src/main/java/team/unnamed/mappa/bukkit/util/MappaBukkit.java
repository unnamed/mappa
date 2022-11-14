package team.unnamed.mappa.bukkit.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.object.Vector;

import java.util.Map;

public interface MappaBukkit {
    org.bukkit.util.Vector ZERO_BUKKIT = new org.bukkit.util.Vector();
    org.bukkit.util.Vector UP_BLOCK_BUKKIT = new org.bukkit.util.Vector(0.5, 1, 0.5);

    static Location toLocation(World world, Vector vector) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    static org.bukkit.util.Vector toBukkit(Vector vector) {
        return new org.bukkit.util.Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    static Vector toMappa(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    static Vector toMappa(Location loc) {
        return new Vector(loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch(),
            true,
            false,
            false);
    }

    static Vector toMappaBlock(Location loc) {
        return new Vector(loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ(),
            loc.getYaw(),
            loc.getPitch(),
            true,
            false,
            false);
    }

    static Vector toMappaVector(Block block, float yaw, float pitch) {
        return new Vector(block.getX(), block.getY(), block.getZ(), yaw, pitch);
    }

    static Vector toMappaVector(Block block) {
        return toMappaVector(block, 0F, 0F);
    }

    static org.bukkit.Chunk toBukkit(World world, Chunk chunk) {
        return world.getChunkAt(chunk.getX(), chunk.getY());
    }

    static Chunk toMappa(org.bukkit.Chunk chunk) {
        return new Chunk(chunk.getX(), chunk.getZ());
    }

    static Tool.Button toMappa(Action action) {
        switch (action) {
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                return Tool.Button.LEFT;
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                return Tool.Button.RIGHT;
            default:
                return null;
        }
    }

    static team.unnamed.mappa.util.BlockFace toMappa(BlockFace face) {
        switch (face) {
            case WEST:
                return team.unnamed.mappa.util.BlockFace.WEST;
            case EAST:
                return team.unnamed.mappa.util.BlockFace.EAST;
            case NORTH:
                return team.unnamed.mappa.util.BlockFace.NORTH;
            case SOUTH:
                return team.unnamed.mappa.util.BlockFace.SOUTH;
            default:
                return null;
        }
    }

    static Clipboard newClipboard(ClipboardHandler handler,
                                  Player player,
                                  Map<String, MapProperty> propertyMap) {
        Vector reference = toMappaBlock(player.getLocation());
        return handler.newCopyOfProperties(player.getUniqueId(),
            reference,
            propertyMap);
    }

    static Location getBlockLoc(Player player) {
        Location location = player.getLocation();
        return new Location(location.getWorld(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            location.getYaw(),
            location.getPitch());
    }
}
