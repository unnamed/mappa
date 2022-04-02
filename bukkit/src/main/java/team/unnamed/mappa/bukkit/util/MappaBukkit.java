package team.unnamed.mappa.bukkit.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.Vector;

public interface MappaBukkit {

    static Location toLocation(World world, Vector vector) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    static org.bukkit.util.Vector toBukkit(Vector vector) {
        return new org.bukkit.util.Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    static Vector toMappa(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    static Vector toMappaVector(Block block) {
        return new Vector(block.getX(), block.getY(), block.getZ());
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
                return Tool.Button.LEFT;
            case RIGHT_CLICK_BLOCK:
                return Tool.Button.RIGHT;
            default:
                return null;
        }
    }
}
