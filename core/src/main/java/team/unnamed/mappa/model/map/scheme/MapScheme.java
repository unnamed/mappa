package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;
import java.util.function.Function;

public interface MapScheme {
    String DEFAULT_FORMAT_NAME = "{map_name}";
    Key<String> SESSION_ID_PATH = new Key<>("session-id");

    static MapSchemeFactory factory(MappaInjector injector) {
        return new MapSchemeFactoryImpl(injector);
    }

    MapEditSession newSession(String id);

    MapEditSession resumeSession(String id, Map<String, Object> properties) throws ParseException;

    String getName();

    String getFormatName();

    String[] getAliases();

    <T> T getObject(Key<T> key);

    <T> T getObject(Key<T> key, Function<String, T> provide);

    Map<String, MapProperty> getProperties();

    Map<String, Object> getParseConfiguration();
}
