package team.unnamed.mappa.bukkit.render;

import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.model.visualizer.Render;
import team.unnamed.mappa.object.Vector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.WeakHashMap;

public abstract class BukkitRender<T> implements Render<Player, T> {
    public static final double MAX_DIRECTION_DISTANCE = 0.75;

    protected final Particles_1_8 particles;
    protected final Class<T> type;
    protected Map<World, Map<org.bukkit.util.Vector, Object>> cached = new WeakHashMap<>();

    protected BukkitRender(Class<T> type, Particles_1_8 particles) {
        this.type = type;
        this.particles = particles;
    }

    /**
     * Build all particle packets to send in one tick.
     *
     * @param entity entity reference
     * @param object object type.
     */
    public abstract void constructPackets(Player entity, T object);

    public void render(Player entity, T object, int radius, boolean renovate) {
        if (renovate) {
            cached = new WeakHashMap<>();
            constructPackets(entity, object);
        }

        sendPackets(entity, radius);
    }

    public void sendPackets(Player entity, int radius) {
        Map<org.bukkit.util.Vector, Object> packets = cached.get(entity.getWorld());
        if (packets == null) {
            return;
        }

        int radiusSquared = radius * radius;
        org.bukkit.util.Vector playerPos = entity.getLocation().toVector();
        for (Map.Entry<org.bukkit.util.Vector, Object> entry : packets.entrySet()) {
            org.bukkit.util.Vector particlePos = entry.getKey();
            Object packet = entry.getValue();
            int distance = (int) particlePos.distanceSquared(playerPos);
            if (distance > radiusSquared) {
                continue;
            }

            sendPacket(entity, packet);
        }
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

    protected Object colouredDust(org.bukkit.util.Vector vector, Color color) {
        return particles.REDSTONE()
            .packetColored(false, vector, color);
    }

    protected void cacheVector(Player player, Vector vector, Color color) {
        org.bukkit.util.Vector bukkit = MappaBukkit.toBukkit(vector);
        if (vector.isBlock()) {
            double x = Math.floor(bukkit.getX()) + 0.5;
            double y = Math.floor(bukkit.getY()) + 0.95;
            double z = Math.floor(bukkit.getZ()) + 0.5;

            bukkit.setX(x);
            bukkit.setY(y);
            bukkit.setZ(z);
        }

        World world = player.getWorld();
        if (vector.isYawPitch()) {
            org.bukkit.util.Vector eyeVector = bukkit
                .clone()
                .add(new org.bukkit.util.Vector(0, player.getEyeHeight(true), 0));
            org.bukkit.util.Vector direction = eyeVector
                .toLocation(world, (float) vector.getYaw(), (float) vector.getPitch())
                .getDirection();

            double component = 0.15;
            for (double d = 0; d < MAX_DIRECTION_DISTANCE; d += component) {
                org.bukkit.util.Vector position = eyeVector.clone()
                    .add(direction.clone().multiply(d));
                Object packet;
                if (d + component == MAX_DIRECTION_DISTANCE) {
                    packet = particles.REDSTONE()
                        .packetColored(false, position, color);
                } else {
                    packet = particles.ENCHANTMENT_TABLE()
                        .packetMotion(false, position, MappaBukkit.ZERO_BUKKIT);
                }
                Map<org.bukkit.util.Vector, Object> packets = getPacketsOf(world);
                packets.put(position, packet);
            }
        }
        cacheColouredDust(world, bukkit, color);
    }

    protected void cacheColouredDust(World world, org.bukkit.util.Vector vector, Color color) {
        Map<org.bukkit.util.Vector, Object> packets = getPacketsOf(world);
        packets.put(vector, colouredDust(vector, color));
    }

    protected void sendPacket(Player player, Object packet) {
        particles.sendPacket(player, packet);
    }

    protected Map<org.bukkit.util.Vector, Object> getPacketsOf(World world) {
        return cached.computeIfAbsent(world, key -> new LinkedHashMap<>());
    }
}
