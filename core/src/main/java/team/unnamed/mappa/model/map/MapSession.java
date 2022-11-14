package team.unnamed.mappa.model.map;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.Storage;
import team.unnamed.mappa.object.MapTypeIterator;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.throwable.FindException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface MapSession extends Iterable<MapProperty>, Storage {

    void setId(String id);

    void setWarning(boolean warning);

    MapSession cleanProperty(String propertyName) throws ParseException;

    boolean removePropertyValue(String propertyName, Object value) throws ParseException;

    boolean containsProperty(String property);

    MapSession property(String property, Object value) throws ParseException;

    boolean setup();

    String currentSetup();

    String skipSetup();

    Map<String, Text> checkWithScheme() throws ParseException;

    Map<String, Text> checkWithScheme(boolean failFast) throws ParseException;

    String getId();

    String getDate();

    String getMapName();

    String getWorldName();

    String getVersion();

    Collection<String> getAuthors();

    String getSchemeName();

    Map<String, Object> getRawProperties();

    MapScheme getScheme();

    MapPropertyTree getProperties();

    MapProperty getProperty(String node);

    MapProperty getCheckProperty(String node) throws FindException;

    boolean isWarning();

    @NotNull
    @Override
    default Iterator<MapProperty> iterator() {
        return new MapTypeIterator<>(MapProperty.class, getRawProperties());
    }
}
