package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.BlockFace;

public class CuboidTransform implements PositionTransform<Cuboid> {

    @Override
    public Cuboid rotate(Cuboid cuboid, boolean mirrored, BlockFace base, BlockFace toFace) {
        Vector pos1 = VectorTransform.rotateVec(cuboid.getMaximum(), mirrored, base, toFace);
        Vector pos2 = VectorTransform.rotateVec(cuboid.getMinimum(), mirrored, base, toFace);
        return new Cuboid(pos1, pos2);
    }

    @Override
    public Cuboid toRelative(Vector reference, Cuboid cuboid) {
        Vector pos1 = VectorTransform.toRelativeVec(reference, cuboid.getMaximum());
        Vector pos2 = VectorTransform.toRelativeVec(reference, cuboid.getMinimum());
        return new Cuboid(pos1, pos2);
    }

    @Override
    public Cuboid toReal(Vector center, Cuboid cuboid) {
        Vector pos1 = VectorTransform.toRealVec(center, cuboid.getMaximum());
        Vector pos2 = VectorTransform.toRealVec(center, cuboid.getMinimum());
        return new Cuboid(pos1, pos2);
    }
}
