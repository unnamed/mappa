package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.annotated.part.AbstractModule;
import me.fixeddev.commandflow.annotated.part.Key;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import me.fixeddev.commandflow.part.CommandPart;
import org.bukkit.World;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Vector;

import java.io.File;
import java.util.function.BiFunction;

public class MappaBukkitPartModule extends AbstractModule {
    private final MappaPlugin plugin;

    public MappaBukkitPartModule(MappaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure() {
        // Call plugin.getBootstrap() after bootstrap is created
        bindRegistry(Cuboid.class, CuboidPlayerPart::new);
        bindRegistry(ChunkCuboid.class, ChunkCuboidPlayerPart::new);
        bindRegistry(Chunk.class, ChunkPlayerPart::new);
        bindRegistry(Vector.class, VectorPlayerPart::new);

        bindFactory(MapScheme.class, (name, registry) -> new MapSchemePart(name, plugin.getBootstrap().getSchemeRegistry()));
        bindFactory(File.class, (name, modifiers) -> new FilePart(name, plugin.getDataFolder()));
        bindFactory(new Key(World.class, Sender.class),
            (name, modifiers) -> new WorldPlayerPart(name));
    }

    public void bindRegistry(Class<?> clazz, BiFunction<String, RegionRegistry, CommandPart> function) {
        bindFactory(clazz,
            (name, list) -> function.apply(
                name, plugin.getBootstrap().getRegionRegistry()));
    }
}