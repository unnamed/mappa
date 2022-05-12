package team.unnamed.mappa.bukkit.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.util.BlockIterator;

public interface BlockUtils {

    static Block getHitBlockOf(Projectile projectile) {
        BlockIterator iterator = new BlockIterator(projectile.getWorld(),
            projectile.getLocation().toVector(),
            projectile.getVelocity().normalize(),
            0.0D,
            3);
        while (iterator.hasNext()) {
            Block next = iterator.next();
            if (next.getType() == Material.AIR) {
                continue;
            }

            return next;
        }
        return null;
    }
}
