package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.model.map.injector.MappaInjector;
import team.unnamed.mappa.model.map.property.MapProperty;

import java.util.Map;

public class DefaultMapScheme implements MapScheme {
    protected final MappaInjector injector;

    protected final String name;
    protected final Map<String, MapProperty> properties;
    protected final Map<String, Object> parseConfiguration;

    public DefaultMapScheme(MappaInjector injector,
                            ParseContext context) {
        this(injector, context.getSchemeName(), context.getProperties(), context.getParseConfiguration());
    }

    public DefaultMapScheme(MappaInjector injector,
                            String name,
                            Map<String, MapProperty> properties,
                            Map<String, Object> parseConfiguration) {
        this.injector = injector;
        this.name = name;
        this.properties = properties;
        this.parseConfiguration = parseConfiguration;
    }

    @Override
    public Map<String, MapProperty> getProperties() {
        return properties;
    }

    @Override
    public Map<String, Object> getParseConfiguration() {
        return parseConfiguration;
    }

    @Override
    public String toString() {
        return "DefaultMapScheme{" +
            "injector=" + injector +
            ", properties=" + properties +
            ", parseConfiguration=" + parseConfiguration +
            '}';
    }
}
