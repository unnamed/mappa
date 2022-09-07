package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.annotated.part.AbstractModule;
import me.fixeddev.commandflow.annotated.part.Key;
import me.fixeddev.commandflow.bukkit.annotation.Sender;
import me.fixeddev.commandflow.part.CommandPart;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.internal.command.parts.MapSerializedSessionPart;
import team.unnamed.mappa.internal.command.parts.MapSessionPart;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
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

        bindBootstrap(MapSession.class, MapSessionPart::new);
        bindBootstrap(MapSerializedSession.class, MapSerializedSessionPart::new);
        bindBootstrap(MapScheme.class, (name, bootstrap) -> new MapSchemePart(name, bootstrap.getSchemeRegistry()));
        bindFactory(new Key(String.class, Path.class), (name, modifiers) -> {
            Path annotation = (Path) modifiers.get(0);
            return new MapPropertyPathPart(name, plugin.getBootstrap(), annotation.findAll());
        });

        bindFactory(File.class, (name, modifiers) -> new FilePart(name, plugin.getDataFolder()));
        bindFactory(MapEditSession.class,
            (name, modifiers) -> new MapEditSessionBukkitPart(name, false, plugin.getBootstrap()));
        bindFactory(new Key(MapEditSession.class, Sender.class),
            (name, modifiers) -> new MapEditSessionBukkitPart(name, true, plugin.getBootstrap()));
    }

    public void bindBootstrap(Key key, BiFunction<String, MappaBootstrap, CommandPart> function) {
        bindFactory(key,
            (name, list) -> function.apply(
                name, plugin.getBootstrap()));
    }

    public void bindBootstrap(Class<?> clazz, BiFunction<String, MappaBootstrap, CommandPart> function) {
        bindFactory(clazz,
            (name, list) -> function.apply(
                name, plugin.getBootstrap()));
    }

    public void bindRegistry(Class<?> clazz, BiFunction<String, RegionRegistry, CommandPart> function) {
        bindFactory(clazz,
            (name, list) -> function.apply(
                name, plugin.getRegionRegistry()));
    }
}
