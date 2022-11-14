package team.unnamed.mappa.object;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseBiConsumer;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.BlockFace;

import java.util.function.BiConsumer;

public interface Clipboard {

    void forEachRealPos(BlockFace face,
                        Vector center,
                        boolean mirrored,
                        ParseBiConsumer<String, Object> consumer) throws ParseException;

    void paste(BlockFace facing,
               Vector center,
               boolean mirrored,
               MapSession session) throws ParseException;

    void paste(BlockFace facing,
               Vector center,
               boolean mirrored,
               MapSession session,
               @Nullable BiConsumer<String, MapProperty> iteration) throws ParseException;

    void castPaste(BlockFace facing,
                   Vector center,
                   boolean mirrored,
                   MapSession session,
                   String toCastPath) throws ParseException;

    void castPaste(BlockFace facing,
                   Vector center,
                   boolean reflect,
                   MapSession session,
                   String toCastPath,
                   @Nullable BiConsumer<String, MapProperty> iteration) throws ParseException;

    boolean isEmpty();

    BlockFace getFacing();
}
