package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.annotated.part.AbstractModule;
import team.unnamed.mappa.internal.command.parts.ChunkCuboidPart;
import team.unnamed.mappa.internal.command.parts.ChunkPart;
import team.unnamed.mappa.internal.command.parts.CuboidPart;
import team.unnamed.mappa.internal.command.parts.VectorPart;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Vector;

public class MappaPartModule extends AbstractModule {

    @Override
    public void configure() {
        bindFactory(Vector.class, (name, modifiers) -> new VectorPart(name));
        bindFactory(Cuboid.class, (name, modifiers) -> new CuboidPart(name));
        bindFactory(Chunk.class, (name, modifiers) -> new ChunkPart(name));
        bindFactory(ChunkCuboid.class, (name, modifiers) -> new ChunkCuboidPart(name));
    }
}
