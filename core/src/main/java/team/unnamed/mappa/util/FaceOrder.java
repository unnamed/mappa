package team.unnamed.mappa.util;

/**
 * Block face order.
 */
public enum FaceOrder {
    NORTH(BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH),
    EAST(BlockFace.SOUTH, BlockFace.NORTH, BlockFace.WEST),
    SOUTH(BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH),
    WEST(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST),
    ;

    public static final FaceOrder[] VALUES = values();
    private final BlockFace next90;
    private final BlockFace previous270;
    private final BlockFace opposite180;

    FaceOrder(BlockFace next90, BlockFace previous270, BlockFace opposite180) {
        this.next90 = next90;
        this.previous270 = previous270;
        this.opposite180 = opposite180;
    }

    public static FaceOrder of(BlockFace face) {
        return VALUES[face.ordinal()];
    }

    public boolean isNext(BlockFace face) {
        return getNext90() == face;
    }

    public boolean isPrevious(BlockFace face) {
        return getPrevious270() == face;
    }

    public boolean isOpposite(BlockFace face) {
        return getOpposite180() == face;
    }

    public BlockFace getNext90() {
        return next90;
    }

    public BlockFace getPrevious270() {
        return previous270;
    }

    public BlockFace getOpposite180() {
        return opposite180;
    }
}
