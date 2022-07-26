package team.unnamed.mappa.internal.mapper;

import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public interface SchemeMapper {

    static Map<String, Object> plainMap(Map<String, Object> map) {
        Map<String, Object> plainMap = new LinkedHashMap<>();
        plainMap("", map, plainMap);
        return plainMap;
    }

    @SuppressWarnings("unchecked")
    static void plainMap(String path,
                         Map<String, Object> map,
                         Map<String, Object> toWrite) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String absolutePath = path;
            if (!absolutePath.isEmpty()) {
                absolutePath += "." + key;
            } else {
                absolutePath = key;
            }
            if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) value;
                plainMap(absolutePath, subMap, toWrite);
                continue;
            }

            toWrite.put(absolutePath, value);
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
