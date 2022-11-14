package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.object.Vector;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public interface ClipboardHandler {

    default <T> void registerTypeTransform(Class<T> clazz,
                                           RealRelative<T> copy,
                                           RelativeReal<T> paste,
                                           Rotation<T> rotation) {
        registerTypeTransform(clazz, new PositionTransformImpl<>(copy, paste, rotation));
    }

    <T> void registerTypeTransform(Class<T> clazz, PositionTransform<T> transform);

    void unregisterAll();

    @SuppressWarnings("unchecked")
    default <T> PositionTransform<T> getTransformOf(Class<T> type) {
        return (PositionTransform<T>) getTransformOf((Type) type);
    }

    default Clipboard newCopyOfProperties(MappaPlayer player,
                                          Map<String, MapProperty> propertyMap) {
        return newCopyOfProperties(player.getUniqueId(), player.getPosition(), propertyMap);
    }

    Clipboard newCopyOfProperties(UUID uuid,
                                  Vector reference,
                                  Map<String, MapProperty> propertyMap);

    PositionTransform<?> getTransformOf(Type type);

    default Clipboard getClipboardOf(MappaPlayer player) {
        return getClipboardOf(player.getUniqueId());
    }

    Clipboard getClipboardOf(UUID uuid);

    Map<UUID, Clipboard> getClipboardMap();

    Map<Type, PositionTransform<?>> getTransforms();
}
