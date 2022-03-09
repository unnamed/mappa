package team.unnamed.mappa.internal;

import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface SchemeMapper {

    Map<String, Object> load(File file) throws InvalidFormatException;

    void saveTo(File file, Map<String, MapProperty> properties) throws IOException;
}
