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
        // Sum +1 for offset rotation
        if (order.isNext(toFace)) {
            degrees = 90;
            x = -vector.getZ();
            z = vector.getX();
            if (!vector.isBlock()) {
                x += 1;
            }
        } else if (order.isOpposite(toFace)) {
            degrees = 180;
            x = -vector.getX();
            z = -vector.getZ();
            if (!vector.isBlock()) {
                x += 1;
                z += 1;
            }
        } else if (order.isPrevious(toFace)) {
            degrees = 270;
            x = vector.getZ();
            z = -vector.getX();
            if (!vector.isBlock()) {
                z += 1;
            }
        } else {
            degrees = 0;
            x = vector.getX();
            z = vector.getZ();
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

    public static Vector toRealVec(Vector center, Vector relative) {
        return relative.sum(center);
    }
}
