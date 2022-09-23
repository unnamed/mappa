package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.object.ClipboardImpl;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.BlockFace;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClipboardHandlerImpl implements ClipboardHandler {
    protected final Map<UUID, Clipboard> clipboardMap;
    protected final Map<Type, PositionTransform<?>> transforms = new HashMap<>();

    public ClipboardHandlerImpl(Map<UUID, Clipboard> clipboardMap) {
        this.clipboardMap = clipboardMap;
    }

    @Override
    public <T> void registerTypeTransform(Class<T> clazz, PositionTransform<T> transform) {
        transforms.put(clazz, transform);
    }

    @Override
    public void unregisterAll() {
        clipboardMap.clear();
        transforms.clear();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Clipboard newCopyOfProperties(UUID uuid,
                                         Vector reference,
                                         Map<String, MapProperty> propertyMap) {
        Map<String, Object> values = new HashMap<>();
        propertyMap.forEach((path, property) -> {
            Object value = property.getValue();
            if (value == null) {
                return;
            }

            PositionTransform transform = transforms.get(property.getType());
            if (transform == null) {
                return;
            }

            values.put(path, transform.toRelative(reference, value));
        });

        Clipboard clipboard = new ClipboardImpl(
            BlockFace.vectorToFace(reference), values, transforms);
        if (!clipboard.isEmpty()) {
            clipboardMap.put(uuid, clipboard);
        }
        return clipboard;
    }

    @Override
    public PositionTransform<?> getTransformOf(Type type) {
        return transforms.get(type);
    }

    @Override
    public Clipboard getClipboardOf(UUID uuid) {
        return clipboardMap.get(uuid);
    }

    @Override
    public Map<UUID, Clipboard> getClipboardMap() {
        return clipboardMap;
    }

    @Override
    public Map<Type, PositionTransform<?>> getTransforms() {
        return transforms;
    }
}
