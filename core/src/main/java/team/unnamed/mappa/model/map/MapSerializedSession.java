package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public final class MapSerializedSession implements MapSession {
    public enum Reason {
        WARNING("RED"),
        BLACK_LIST("LIGHT_PURPLE"),
        DUPLICATE("DARK_GRAY");

        private final String colorName;

        Reason(String colorName) {
            this.colorName = colorName;
        }

        public String getColorName() {
            return colorName;
        }

        public TranslationNode asTextNode() {
            return TranslationNode.valueOf("REASON_" + name());
        }
    }

    private String id;
    private final MapScheme scheme;
    private final Reason reason;
    private boolean warning;
    private final Map<String, Object> serializedProperties;

    public MapSerializedSession(String id,
                                MapScheme scheme,
                                Reason reason,
                                boolean warning,
                                Map<String, Object> serializedProperties) {
        this.id = id;
        this.scheme = scheme;
        this.reason = reason;
        this.warning = warning;
        this.serializedProperties = serializedProperties;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    @Override
    public boolean containsProperty(String property) {
        return serializedProperties.containsKey(property);
    }

    @Override
    public MapSession property(String path, Object value) throws ParseException {
        int aDot = path.indexOf(".");
        boolean found = false;
        if (aDot == -1) {
            if (serializedProperties.containsKey(path)) {
                serializedProperties.put(path, value);
                found = true;
            }
        } else {
            found = iteratePath(serializedProperties,
                path.split("\\."),
                0,
                value);
        }

        if (!found) {
            throw new ParseException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", String.join(".", path),
                        "{scheme}", scheme.getName()));
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public boolean iteratePath(Map<String, Object> properties,
                               String[] path,
                               int index,
                               Object newValue) {
        String segment = path[index];
        Object value = properties.get(segment);
        if (value == null) {
            return false;
        } else if (index + 1 == path.length) {
            Class<?> type = newValue.getClass();
            if (!type.isAssignableFrom(value.getClass())) {
                return false;
            }

            properties.put(segment, newValue);
            return true;
        } else if (!(value instanceof Map)) {
            return false;
        }

        return iteratePath((Map<String, Object>) value, path, ++index, newValue);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSchemeName() {
        return scheme.getName();
    }

    @Override
    public MapScheme getScheme() {
        return scheme;
    }

    @Override
    public boolean isWarning() {
        return warning;
    }

    public Reason getReason() {
        return reason;
    }

    public Map<String, Object> getSerializedProperties() {
        return serializedProperties;
    }
}
