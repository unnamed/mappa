package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.BlockFace;
import team.unnamed.mappa.util.FaceOrder;
import team.unnamed.mappa.util.MathUtils;

public class VectorTransform implements PositionTransform<Vector> {

    @Override
    public Vector rotate(Vector vector, BlockFace base, BlockFace toFace) {
        return rotateVec(vector, base, toFace);
    }

    public static Vector rotateVec(Vector vector, BlockFace base, BlockFace toFace) {
        FaceOrder order = FaceOrder.of(base);
        int degrees;
        double x;
        double z;
        if (order.isNext(toFace)) {
            degrees = 90;
            x = -vector.getZ();
            z = vector.getX();
        } else if (order.isOpposite(toFace)) {
            degrees = 180;
            x = -vector.getX();
            z = -vector.getZ();
        } else if (order.isPrevious(toFace)) {
            degrees = 270;
            z = -vector.getX();
            x = vector.getZ();
        } else {
            throw new IllegalArgumentException("Cannot rotate vector in the same direction!");
        }

        double yaw = MathUtils.fixYaw(vector.getYaw() + degrees);
        return new Vector(x,
            vector.getY(),
            z,
            yaw,
            vector.getPitch(),
            vector.isYawPitch(),
            vector.isNoY(),
            vector.isBlock());
    }

    @Override
    public Vector toRelative(Vector reference, Vector vector) {
        return toRelativeVec(reference, vector);
    }

    public static Vector toRelativeVec(Vector reference, Vector vector) {
        Vector relative = reference
            .distance(vector)
            .setYawPitch(vector.isYawPitch())
            .mutNoY(vector.isNoY())
            .mutBlock(vector.isBlock());
        if (vector.isYawPitch()) {
            relative = relative.mutYawPitch(
                vector.getYaw(), vector.getPitch());
        }
        return relative;
    }

    @Override
    public Vector toReal(Vector center, Vector vector) {
        return toRealVec(center, vector);
    }

    public static Vector toRealVec(Vector center, Vector vector) {
        return vector.sum(center);
    }
}
