package team.unnamed.mappa.object;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.clipboard.PositionTransform;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.ParseBiConsumer;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.throwable.ParseRuntimeException;
import team.unnamed.mappa.util.BlockFace;
import team.unnamed.mappa.util.Texts;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ClipboardImpl implements Clipboard {
    private final BlockFace facing;
    private final Map<Type, PositionTransform<?>> transformMap;
    private final Map<String, Object> relativeMap;

    public ClipboardImpl(BlockFace facing,
                         Map<String, Object> relativeMap,
                         Map<Type, PositionTransform<?>> transformMap) {
        this.facing = facing;
        this.transformMap = transformMap;
        this.relativeMap = relativeMap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void forEachRealPos(BlockFace face,
                               Vector center,
                               ParseBiConsumer<String, Object> consumer) throws ParseException {
        for (Map.Entry<String, Object> entry : relativeMap.entrySet()) {
            String path = entry.getKey();
            Object relative = entry.getValue();
            PositionTransform transform = Objects.requireNonNull(
                transformMap.get(relative.getClass()),
                "No position transform for type " + relative.getClass().getSimpleName()
            );

            if (face != facing) {
                relative = transform.rotate(relative, facing, face);
            }
            Object real = transform.toReal(center, relative);
            consumer.accept(path, real);
        }
    }

    @Override
    public void paste(BlockFace facing,
                      Vector center,
                      MapEditSession session) throws ParseException {
        paste(facing, center, session, null);
    }

    @Override
    public void paste(BlockFace facing,
                      Vector center,
                      MapEditSession session,
                      @Nullable BiConsumer<String, MapProperty> iteration) throws ParseException {
        forEachRealPos(facing,
            center,
            (path, value) -> {
                MapProperty property = session.getProperty(path);
                try {
                    property.parseValue(value);
                    if (iteration != null) {
                        iteration.accept(path, property);
                    }
                } catch (ParseRuntimeException e) {
                    throw new ArgumentTextParseException(e.getTextNode());
                }
            });
    }

    @Override
    public void castPaste(BlockFace facing,
                          Vector center,
                          MapEditSession session,
                          String toCastPath) throws ParseException {
        castPaste(facing, center, session, toCastPath, null);
    }

    @Override
    public void castPaste(BlockFace facing,
                          Vector center,
                          MapEditSession session,
                          String toCastPath,
                          @Nullable BiConsumer<String, MapProperty> iteration) throws ParseException {
        forEachRealPos(facing,
            center,
            (path, value) -> {
                int dot = path.lastIndexOf(".");
                String name = dot == -1 ? path : path.substring(dot + 1);
                String absolutePath = toCastPath + "." + name;
                MapProperty mapProperty = session.getProperty(absolutePath);
                if (mapProperty == null) {
                    throw new ArgumentTextParseException(
                        TranslationNode
                            .INVALID_PROPERTY
                            .withFormal("{property}", absolutePath)
                    );
                }

                Type type = mapProperty.getType();
                if (type != Vector.class) {
                    throw new ArgumentTextParseException(
                        TranslationNode
                            .INVALID_CAST
                            .withFormal("{path}", path,
                                "{cast}", absolutePath,
                                "{type}", Texts.getTypeName(Vector.class),
                                "{conflict}", Texts.getTypeName(type))
                    );
                }

                try {
                    mapProperty.parseValue(value);
                    if (iteration != null) {
                        iteration.accept(absolutePath, mapProperty);
                    }
                } catch (ParseRuntimeException e) {
                    throw new ArgumentTextParseException(e.getTextNode());
                }
            });
    }

    @Override
    public boolean isEmpty() {
        return relativeMap.isEmpty();
    }

    @Override
    public BlockFace getFacing() {
        return facing;
    }
}
