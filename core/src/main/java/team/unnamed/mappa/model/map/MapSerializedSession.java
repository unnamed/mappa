package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.TranslationNode;

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
