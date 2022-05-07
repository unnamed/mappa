package team.unnamed.mappa.internal.mapper;

import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public interface SchemeMapper {

    static Map<String, Object> plainMap(Map<String, Object> map) {
        Map<String, Object> plainMap = new LinkedHashMap<>();
        plainMap("", map, plainMap);
        return plainMap;
    }

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

    Map<String, Object> resumeSession(Map<String, MapScheme> schemeMap,
                                      boolean loadWarning,
                                      Set<String> idBlacklist,
                                      File file) throws ParseException;

    void saveTo(FileWriter writer, MapSession session) throws IOException;

    void serializeTo(FileWriter writer, MapSession session) throws IOException;

    void serializeTo(FileWriter writer, MapSerializedSession session) throws IOException;

    String getFormatFile();

    DuplicationStrategy getDuplicationStrategy();
}
