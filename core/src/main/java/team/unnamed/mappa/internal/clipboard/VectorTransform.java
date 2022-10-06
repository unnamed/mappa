package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.BlockFace;
import team.unnamed.mappa.util.FaceOrder;
import team.unnamed.mappa.util.HorizontalAxis;
import team.unnamed.mappa.util.MathUtils;

public class VectorTransform implements PositionTransform<Vector> {

    @Override
    public Vector rotate(Vector vector, boolean mirrored, BlockFace base, BlockFace toFace) {
        return rotateVec(vector, mirrored, base, toFace);
    }

    public static Vector rotateVec(Vector vector, boolean mirrored, BlockFace base, BlockFace toFace) {
        FaceOrder order = FaceOrder.of(base);
        int degrees;
        double vecX = vector.getX();
        double vecZ = vector.getZ();
        double x;
        double z;
        double yaw = vector.getYaw();
        if (mirrored) {
            BlockFace facing = BlockFace.yawToFace(yaw);
            HorizontalAxis axis = base.getAxis();
            if (axis == HorizontalAxis.Z) {
                vecX = -vecX;
                if (!axis.insideAxis(facing) && !vector.isBlock()) {
                    yaw = MathUtils.fixYaw(yaw + 180);
                    vecX += 1;
                }
            } else {
                vecZ = -vecZ;
                if (!axis.insideAxis(facing) && !vector.isBlock()) {
                    yaw = MathUtils.fixYaw(yaw + 180);
                    vecZ += 1;
                }
            }
        }

        // Sum +1 for offset rotation
        if (order.isNext(toFace)) {
            degrees = 90;
            x = -vecZ;
            z = vecX;
            if (!vector.isBlock()) {
                x += 1;
            }
        } else if (order.isOpposite(toFace)) {
            degrees = 180;
            x = -vecX;
            z = -vecZ;
            if (!vector.isBlock()) {
                x += 1;
                z += 1;
            }
        } else if (order.isPrevious(toFace)) {
            degrees = 270;
            x = vecZ;
            z = -vecX;
            if (!vector.isBlock()) {
                z += 1;
            }
        } else {
            degrees = 0;
            x = vecX;
            z = vecZ;
        }

        if (degrees != 0) {
            yaw = MathUtils.fixYaw(yaw + degrees);
        }

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
