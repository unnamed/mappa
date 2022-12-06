package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.annotated.part.AbstractModule;
import me.fixeddev.commandflow.annotated.part.Key;
import me.fixeddev.commandflow.part.CommandPart;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaPlatform;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.object.Vector;

import java.io.File;
import java.util.function.BiFunction;

public class MappaAPIPartModule extends AbstractModule {
    private final MappaAPI api;

    public MappaAPIPartModule(MappaAPI api) {
        this.api = api;
    }

    @Override
    public void configure() {
        bindRegistry(Cuboid.class, (name, registry) -> new RegionPlayerPart(name,
            registry,
            Cuboid::parse,
            Vector.class));
        bindRegistry(ChunkCuboid.class, (name, registry) -> new RegionPlayerPart(name,
            registry,
            ChunkCuboid::parse,
            Chunk.class));
        bindRegistry(Chunk.class, (name, registry) -> new FirstSelectionPart<>(name,
            registry,
            Chunk::parse,
            Chunk.class));
        bindRegistry(Vector.class, (name, registry) -> new FirstSelectionPart<>(name,
            registry,
            Vector::parse,
            Vector.class));

        bindFactory(MappaPlayer.class, (name, modifiers) -> {
            boolean onlyPlayer = modifiers
                .stream()
                .anyMatch(annotation -> annotation.annotationType() == Sender.class);
            return new MappaPlayerPart(name, onlyPlayer);
        });
        bindPlatform(MapScheme.class, (name, platform) -> new MapSchemePart(name, platform.getMapRegistry()));
        bindFactory(MapSession.class, new MapSessionFactoryPart(api));
        bindPlatform(MappaCommandManager.SESSION_KEY,
            (name, platform) -> new MapSessionSenderPart(name, true, platform));
        bindFactory(new Key(String.class, Path.class), (name, modifiers) -> {
            Path annotation = (Path) modifiers.get(0);
            return new MapPropertyPathPart(name, annotation.find(), annotation.collect());
        });
        bindFactory(new Key(Clipboard.class, Sender.class),
            (name, modifiers) -> new ClipboardSenderPart(name, api.getClipboardHandler()));

        bindFactory(File.class, (name, modifiers) -> new FilePart(name, api.getDataFolder()));
    }

    public void bindPlatform(Key key, BiFunction<String, MappaPlatform, CommandPart> function) {
        bindFactory(key,
            (name, list) -> function.apply(
                name, api.getPlatform()));
    }

    public void bindPlatform(Class<?> clazz, BiFunction<String, MappaPlatform, CommandPart> function) {
        bindFactory(clazz,
            (name, list) -> function.apply(
                name, api.getPlatform()));
    }

    public void bindRegistry(Class<?> clazz, BiFunction<String, RegionRegistry, CommandPart> function) {
        bindFactory(clazz,
            (name, list) -> function.apply(
                name, api.getRegionRegistry()));
    }
}
