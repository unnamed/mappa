package team.unnamed.mappa.bukkit.render;

import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.model.visualizer.Render;
import team.unnamed.mappa.object.Vector;

import java.util.SplittableRandom;

public abstract class BukkitRender<T> implements Render<Player, T> {
    protected final Particles_1_8 particles;
    protected final Class<T> type;

    protected BukkitRender(Class<T> type, Particles_1_8 particles) {
        this.type = type;
        this.particles = particles;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    protected int randomColorInt() {
        return new SplittableRandom().nextInt(0, 255);
    }

    protected Color randomColor() {
        return Color.fromBGR(
            randomColorInt(),
            randomColorInt(),
            randomColorInt()
        );
    }

    public Object colouredDust(org.bukkit.util.Vector vector, Color color) {
        return particles.REDSTONE()
            .packetColored(false, vector, color);
    }

    public void sendParticle(Player player, Vector vector, Color color) {
        org.bukkit.util.Vector bukkit = MappaBukkit.toBukkit(vector);
        if (vector.isBlock()) {
            double x = Math.floor(bukkit.getX()) + 0.5;
            double y = Math.floor(bukkit.getY()) + 0.95;
            double z = Math.floor(bukkit.getZ()) + 0.5;

            bukkit.setX(x);
            bukkit.setY(y);
            bukkit.setZ(z);
        }
        sendParticle(player, bukkit, color);
    }

    public void sendParticle(Player player, org.bukkit.util.Vector vector, Color color) {
        particles.sendPacket(player, colouredDust(vector, color));
    }
}
