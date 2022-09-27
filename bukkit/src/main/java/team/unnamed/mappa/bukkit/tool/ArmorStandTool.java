package team.unnamed.mappa.bukkit.tool;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.BlockFace;
import team.unnamed.mappa.util.MathUtils;
import team.unnamed.mappa.util.Texts;

import java.util.UUID;

public class ArmorStandTool extends AbstractBukkitTool {
    private final JavaPlugin plugin = JavaPlugin.getPlugin(MappaPlugin.class);

    public ArmorStandTool(RegionRegistry regionRegistry, MappaTextHandler textHandler) {
        super(ToolHandler.ARMOR_STAND_TOOL,
            false,
            regionRegistry,
            textHandler,
            Vector.class);
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        org.bukkit.util.Vector bukkit = MappaBukkit.toBukkit(lookingAt);
        World world = entity.getWorld();
        Location location = bukkit.toLocation(world)
            .add(MappaBukkit.UP_BLOCK_BUKKIT);

        org.bukkit.util.Vector direction = entity.getLocation()
            .toVector()
            .subtract(location.toVector())
            .normalize();
        location.setDirection(direction);
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        if (shift) {
            yaw = BlockFace.yawToFace(yaw)
                .oppositeFace()
                .toDegrees();
        }
        location.setYaw(MathUtils.fixYaw(yaw));
        location.setPitch(MathUtils.roundDecimals(pitch));
        ArmorStand stand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);

        UUID uuid = entity.getUniqueId();
        RegionSelection<Vector> selection = regionRegistry
            .getOrNewVectorSelection(uuid.toString());
        lookingAt = MappaBukkit.toMappa(stand.getLocation());
        selection.setFirstPoint(lookingAt);
        Bukkit.getScheduler()
            .runTaskLater(plugin, stand::remove, 3);

        textHandler.send(entity, BukkitTranslationNode
            .FIRST_POINT_SELECTED
            .with("{type}", Texts.getTypeName(Vector.class),
                "{location}", Vector.toString(lookingAt)));
        playSound(entity, XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 3, 4);
    }

    public void playSound(Player entity, XSound sound, int times, int ticks) {
        new BukkitRunnable() {
            private int index;
            private float pitch = 0.5F;

            @Override
            public void run() {
                sound.play(entity, 1, pitch);
                ++index;
                if (index >= times) {
                    cancel();
                } else {
                    pitch *= 2;
                }
            }
        }.runTaskTimer(plugin, 0, ticks);
    }
}
