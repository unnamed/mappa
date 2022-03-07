package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.model.map.injector.MappaInjector;
import team.unnamed.mappa.model.map.property.MapProperty;

import java.util.Map;

public interface MapScheme {

    static MapSchemeFactory factory(MappaInjector injector) {
        return new MapSchemeFactoryImpl(injector);
    }

    Map<String, MapProperty> getProperties();

    Map<String, Object> getParseConfiguration();
}
