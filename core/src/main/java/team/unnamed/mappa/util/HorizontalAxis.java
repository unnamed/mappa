package team.unnamed.mappa.util;

public enum HorizontalAxis {
    X(BlockFace.WEST, BlockFace.EAST),
    Z(BlockFace.NORTH, BlockFace.SOUTH);

    private final BlockFace[] faces;

    HorizontalAxis(BlockFace... faces) {
        this.faces = faces;
    }

    public boolean insideAxis(BlockFace face) {
        for (BlockFace blockFace : faces) {
            if (face == blockFace) {
                return true;
            }
        }
        return false;
    }

    public BlockFace[] getFaces() {
        return faces;
    }
}
