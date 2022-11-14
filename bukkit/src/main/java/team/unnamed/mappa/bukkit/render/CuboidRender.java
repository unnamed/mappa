package team.unnamed.mappa.bukkit.render;

import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import org.bukkit.Color;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Vector;

public class CuboidRender extends BukkitRender<Cuboid> {
    protected final Color color = randomColor();

    public CuboidRender(Particles_1_8 particles) {
        super(Cuboid.class, particles);
    }

    @Override
    public void constructPackets(MappaPlayer entity, Cuboid cuboid) {
        cuboid.forEachCorner(
            (x, y, z) -> cacheVector(entity, new Vector(x, y, z), color));
    }
}
