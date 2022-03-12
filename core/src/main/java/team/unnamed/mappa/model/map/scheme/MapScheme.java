package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public interface MapScheme {

    static MapSchemeFactory factory(MappaInjector injector) {
        return new MapSchemeFactoryImpl(injector);
    }

    MapSession newSession(String worldName);

    MapSession resumeSession(String worldName, Map<String, Object> properties) throws ParseException;

    String getName();

    Map<String, MapProperty> getProperties();

    Map<String, Object> getParseConfiguration();
}
