package team.unnamed.mappa.internal.mapper;

import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public interface SchemeMapper {

    public static void printMap(Map<?, ?> map) {
        printMap(map, 0);
        System.out.println();
    }

    public static void printMap(Map<?, ?> map, int spaces) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            builder.append(' ');
        }
        String spacer = builder.toString();
        if (map == null) {
            System.out.println(spacer + "Map is null!");
            return;
        } else if (map.isEmpty()) {
            System.out.println(spacer + "Map is empty!");
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println(spacer + "key: " + entry.getKey());
            Object value = entry.getValue();
            if (value == null) {
                value = "null";
            } else if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                collection.forEach(object ->
                    System.out.println(spacer + "- " + object + ", type: " + ((object != null ? object.getClass() : null))));
                continue;
            } else if (value instanceof Map) {
                System.out.println(spacer + "-> ");
                printMap((Map<?, ?>) value, spaces + 1);
                continue;
            }
            System.out.println(spacer + "value: " + value);
        }
    }

    Map<String, Object> load(File file) throws ParseException;

    Map<String, Object> loadSessions(MapScheme scheme, File file) throws ParseException;

    Map<String, Object> resumeSessions(Object sender,
                                       MappaBootstrap bootstrap,
                                       boolean loadWarning,
                                       File file)
        throws ParseException;

    void saveTo(File file, MapEditSession session);

    void serializeTo(FileWriter file, MapSession session) throws IOException;

    void applySave(File file) throws IOException;

    String getFormatFile();
}
