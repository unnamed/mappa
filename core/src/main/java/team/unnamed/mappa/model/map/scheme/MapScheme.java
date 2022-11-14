package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public interface MapScheme extends Storage {
    String DEFAULT_FORMAT_NAME = "{map_name}";
    Key<String> SESSION_ID_PATH = new Key<>("session-id");
    Key<Set<String>> IMMUTABLE_SET = new Key<>("immutables");

    Key<Set<String>> PLAIN_KEYS = new Key<>("plain-keys");

    static MapSchemeFactory factory(MappaInjector injector) {
        return new MapSchemeFactoryImpl(injector);
    }

    MapEditSession newSession(String id) throws ParseException;

    MapEditSession resumeSession(String id, Map<String, Object> properties) throws ParseException;

    AtomicInteger getCounter();

    int getAndIncrementCounter();

    String getName();

    String getFormatName();

    String[] getAliases();

    MapPropertyTree getTreeProperties();

    Map<String, Object> getParseConfiguration();
}
