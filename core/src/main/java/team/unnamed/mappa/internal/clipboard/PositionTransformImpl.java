package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.BlockFace;

public class PositionTransformImpl<T> implements PositionTransform<T> {
    private final RealRelative<T> realRelative;
    private final RelativeReal<T> relativeReal;
    private final Rotation<T> rotation;

    public PositionTransformImpl(RealRelative<T> realRelative, RelativeReal<T> relativeReal, Rotation<T> rotation) {
        this.realRelative = realRelative;
        this.relativeReal = relativeReal;
        this.rotation = rotation;
    }

    @Override
    public T toRelative(Vector reference, T t) {
        return realRelative.toRelative(reference, t);
    }

    @Override
    public T toReal(Vector center, T t) {
        return relativeReal.toReal(center, t);
    }

    @Override
    public T rotate(T t, BlockFace base, BlockFace toFace) {
        return rotation.rotate(t, base, toFace);
    }
}
