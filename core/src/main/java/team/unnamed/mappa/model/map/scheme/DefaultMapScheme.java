package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public class DefaultMapScheme implements MapScheme {
    protected final MappaInjector injector;

    protected final String name;
    protected final Map<String, MapProperty> properties;
    protected final Map<String, Object> parseConfiguration;

    protected final String formatName;
    protected final String[] aliases;

    public DefaultMapScheme(MappaInjector injector,
                            ParseContext context) {
        this(injector, context.getSchemeName(), context.getProperties(), context.getParseConfiguration());
    }

    @SuppressWarnings("unchecked")
    public DefaultMapScheme(MappaInjector injector,
                            String name,
                            Map<String, MapProperty> properties,
                            Map<String, Object> parseConfiguration) {
        this.injector = injector;
        this.name = name;
        this.properties = properties;
        this.parseConfiguration = parseConfiguration;

        Map<String, Object> parentConfig =
            (Map<String, Object>) this.parseConfiguration.get(
                NodeParentParseConfiguration.PARENT_CONFIGURATION);
        if (parentConfig == null) {
            this.formatName = DEFAULT_FORMAT_NAME;
            this.aliases = null;
            return;
        }

        String formatName = (String) parentConfig.get("format-name");
        this.formatName = formatName == null || formatName.isEmpty()
            ? DEFAULT_FORMAT_NAME
            : formatName;
        this.aliases = (String[]) parentConfig.get("aliases");
    }

    @Override
    public MapSession newSession(String worldName) {
        return new MapSession(worldName, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapSession resumeSession(String worldName, Map<String, Object> source) throws ParseException {
        MapSession session = newSession(worldName);
        Map<String, Object> sourceNode = (Map<String, Object>) source.get(worldName);
        for (Map.Entry<String, Object> entry : sourceNode.entrySet()) {
            session.property(entry.getKey(), entry.getValue());
        }
        return session;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormatName() {
        return formatName;
    }

    @Override
    public String[] getAliases() {
        return aliases;
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
