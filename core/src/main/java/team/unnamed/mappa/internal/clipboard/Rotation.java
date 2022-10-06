package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.util.BlockFace;

public interface Rotation<T> {

    T rotate(T t, boolean mirrored, BlockFace base, BlockFace toFace);
}
