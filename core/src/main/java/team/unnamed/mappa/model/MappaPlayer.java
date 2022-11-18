package team.unnamed.mappa.model;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.Clipboard;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface MappaPlayer {

    void send(String message);

    void send(String message, boolean formal);

    void send(Text text);

    void send(Text text, Object... entities);

    String format(String node);

    String format(Text text);

    String format(Text text, Object... entities);

    default void send(Collection<Text> texts, Object... entities) {
        for (Text text : texts) {
            send(text, entities);
        }
    }

    void selectMapSession(MapSession session);

    void deselectMapSession();

    default void selectVector(Vector vector, RegionSelection.Order order) {
        selectRegion(vector, order);
    }

    default void selectChunk(Chunk chunk, RegionSelection.Order order) {
        selectRegion(chunk, order);
    }

    <R> void selectRegion(R object, RegionSelection.Order order);

    void setProperty(String path, Object value) throws ParseException;

    default void showVisual(String path) {
        showVisual(path, true);
    }

    void showVisual(String path, boolean notify);

    default void hideVisual(String path) {
        hideVisual(path, true);
    }

    void hideVisual(String path, boolean notify);

    boolean hasVisual(String path);

    boolean hasPermission(String permission);

    Vector getPosition(boolean block);

    default Vector getPosition() {
        return getPosition(true);
    }

    void removePropertyValue(String path, Object value) throws ParseException;

    void clearProperty(String path);

    void clearProperty(String path, MapProperty property);

    void showSessionInfo(@Nullable MapSession session);

    void showSessionList();

    void showPropertyInfo(String path);

    void showPropertyInfo(String path, MapProperty property);

    void showSetup() throws ParseException;

    void verifyMapSession(boolean showAll) throws ParseException;

    Clipboard copy(Map<String, MapProperty> properties);

    void clearClipboard();

    void paste(boolean mirror) throws ParseException;

    void castPaste(String path, boolean mirror) throws ParseException;

    Clipboard getClipboard();

    MapSession getMapSession();

    UUID getUniqueId();

    String getWorldName();

    default boolean isConsole() {
        return getUniqueId() == null;
    }

    Object asEntity();

    @SuppressWarnings("unchecked")
    default <T> T cast() {
        return (T) asEntity();
    }

    void flush();
}
