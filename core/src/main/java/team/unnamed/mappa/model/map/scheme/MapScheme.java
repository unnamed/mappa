package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public interface MapScheme {
    String DEFAULT_FORMAT_NAME = "{map_name}";

    static MapSchemeFactory factory(MappaInjector injector) {
        return new MapSchemeFactoryImpl(injector);
    }

    MapEditSession newSession(String id);

    MapEditSession resumeSession(String id, Map<String, Object> properties) throws ParseException;

    String getName();

    String getFormatName();

    String[] getAliases();

    Map<String, MapProperty> getProperties();

    Map<String, Object> getParseConfiguration();
}
