package team.unnamed.mappa.internal.mapper;

import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public interface SchemeMapper {

    Map<String, Object> load(File file) throws ParseException;

    Map<String, Object> loadSessions(MapScheme scheme, File file) throws ParseException;

    void saveTo(FileWriter writer, MapSession session) throws IOException;

    String getFormatFile();
}
