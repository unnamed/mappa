package team.unnamed.mappa.bukkit.render;

import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import team.unnamed.mappa.object.Vector;

public class VectorRender extends BukkitRender<Vector> {
    protected final Color color = randomColor();

    public VectorRender(Particles_1_8 particles) {
        super(Vector.class, particles);
    }

    @Override
    public void constructPackets(Player entity, Vector vector) {
        cacheVector(entity, vector, color);
    }
}
