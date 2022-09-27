package team.unnamed.mappa.util;

import team.unnamed.mappa.object.Vector;

/**
 * BlockFace enum based on bukkit's BlockFace.
 * Used to represent faces of Minecraft blocks and more.
 *
 * <br>
 * Yaw-to-block-face utils are part of a spigot's forum thread, but idk who is the real author
 */
public enum BlockFace {
    NORTH(180),
    EAST(270),
    SOUTH(0),
    WEST(90);

    private final int degrees;
    public static final BlockFace[] VALUES = {NORTH, EAST, SOUTH, WEST};

    BlockFace(int degrees) {
        this.degrees = degrees;
    }

    /**
     * Convert yaw to block face.
     *
     * @param yaw Yaw to convert.
     * @return Block face of yaw.
     */
    public static BlockFace yawToFace(float yaw) {
        return VALUES[Math.round(yaw / 90f) & 0x3];
    }

    /**
     * Get block face of vector.
     *
     * @param vector Vector to get block face.
     * @return Block face or null if vector doesn't have yaw.
     */
    public static BlockFace vectorToFace(Vector vector) {
        return !vector.isYawPitch() ? null : yawToFace((float) vector.getYaw());
    }

    public BlockFace oppositeFace() {
        switch (this) {
            case WEST:
                return EAST;
            case EAST:
                return WEST;
            case NORTH:
                return SOUTH;
            default:
            case SOUTH:
                return NORTH;
        }
    }

    public float toDegrees() {
        return degrees;
    }

    public int getDegrees() {
        return degrees;
    }
}
