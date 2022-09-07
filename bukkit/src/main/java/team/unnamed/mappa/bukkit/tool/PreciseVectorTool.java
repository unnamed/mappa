package team.unnamed.mappa.bukkit.tool;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import team.unnamed.mappa.bukkit.util.BlockUtils;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.bukkit.util.MathUtils;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.object.Vector;

import java.util.Map;
import java.util.function.Consumer;

public class PreciseVectorTool extends VectorTool {
    protected final Map<Integer, Consumer<Projectile>> projectileCache;

    public PreciseVectorTool(Map<Integer, Consumer<Projectile>> projectileCache,
                             RegionRegistry regionRegistry,
                             MappaTextHandler textHandler) {
        super(ToolHandler.PRECISE_VECTOR_TOOL,
            true,
            regionRegistry,
            textHandler);
        this.projectileCache = projectileCache;
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        Location location = entity.getLocation();
        Arrow arrow = entity.launchProjectile(Arrow.class, location.getDirection());
        arrow.setCritical(false);
        arrow.spigot().setDamage(0D);
        projectileCache.put(arrow.getEntityId(),
            projectile -> {
                Location arrowLocation = projectile.getLocation();
                Vector arrowHit = MappaBukkit.toMappa(arrowLocation.toVector());
                arrowHit = MathUtils.roundVector(arrowHit);

                if (shift) {
                    Block hitBlock = BlockUtils.getHitBlockOf(arrow);
                    if (hitBlock != null) {
                        Block block = arrowLocation.getBlock();
                        BlockFace face = hitBlock.getFace(block);
                        if (face == BlockFace.UP) {
                            int y = hitBlock.getY();
                            arrowHit = arrowHit.mutY(++y); // ++y to block floor
                        }
                    }
                }

                super.interact(entity,
                    arrowHit,
                    button,
                    false); // We don't use shift because here we already used
            }
        );
    }
}
