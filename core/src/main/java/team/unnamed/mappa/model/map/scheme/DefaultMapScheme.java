package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public class DefaultMapScheme implements MapScheme {

    protected final String name;
    protected final Map<String, MapProperty> properties;
    protected final Map<String, Object> parseConfiguration;

    protected final String formatName;
    protected final String[] aliases;

    public DefaultMapScheme(ParseContext context) {
        this(context.getSchemeName(), context.getProperties(), context.getParseConfiguration());
    }

    @SuppressWarnings("unchecked")
    public DefaultMapScheme(String name,
                            Map<String, MapProperty> properties,
                            Map<String, Object> parseConfiguration) {
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
    public MapEditSession newSession(String id) {
        return new MapEditSession(id, this);
    }

    @Override
    public MapEditSession resumeSession(String id, Map<String, Object> source) throws ParseException {
        MapEditSession session = newSession(id);
        for (Map.Entry<String, Object> entry : source.entrySet()) {
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
    public <T> T getObject(Key<T> key) {
        return (T) parseConfiguration.get(key.getName());
    }

    @Override
    public <T> T getObject(Key<T> key, Function<String, T> provide) {
        return (T) parseConfiguration.computeIfAbsent(key.getName(), provide);
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
            "properties=" + properties +
            ", parseConfiguration=" + parseConfiguration +
            '}';
    }
}
