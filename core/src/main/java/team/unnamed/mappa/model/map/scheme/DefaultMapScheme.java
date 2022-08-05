package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class DefaultMapScheme implements MapScheme {

    protected final String name;
    protected final MapPropertyTree properties;
    protected final Map<String, Object> parseConfiguration;

    protected final String formatName;
    protected final String[] aliases;

    public DefaultMapScheme(ParseContext context) {
        this(context.getSchemeName(), context.getTreeProperties(), context.getParseConfiguration());
    }

    public DefaultMapScheme(String name,
                            MapPropertyTree properties,
                            Map<String, Object> parseConfiguration) {
        this.name = name;
        this.properties = properties;
        this.parseConfiguration = parseConfiguration;

        Map<String, Object> parentConfig =
            getObject(NodeParentParseConfiguration.PARENT_CONFIGURATION);
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
    public MapEditSession newSession(String id) throws ParseException {
        return new MapEditSession(id, this);
    }

    @Override
    public MapEditSession resumeSession(String id, Map<String, Object> source) throws ParseException {
        MapEditSession session = newSession(id);
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String propertyName = entry.getKey();
            MapProperty property = session.getProperty(propertyName);
            if (property == null) {
                throw new InvalidPropertyException(
                    TranslationNode
                        .INVALID_PROPERTY
                        .with("{property}", propertyName,
                            "{scheme}", this.name));
            }

            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            if (property.isImmutable()) {
                property.bypassParseValue(value);
            } else {
                property.parseValue(value);
            }
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
    public MapPropertyTree getTreeProperties() {
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
