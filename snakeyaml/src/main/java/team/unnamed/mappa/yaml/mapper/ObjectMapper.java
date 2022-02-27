package team.unnamed.mappa.yaml.mapper;

import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.throwable.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface ObjectMapper {

    Map<String, Object> load(File file) throws InvalidFormatException;

    void saveTo(File file, Map<String, Object> mapped) throws IOException;
}
