package team.unnamed.mappa.model.map;

import java.util.Map;

public final class MapSerializedSession {
    public enum Reason {WARNING, BLACK_LIST, DUPLICATE}

    private String id;
    private final String schemeName;
    private final Reason reason;
    private final boolean warning;
    private final Map<String, Object> properties;

    public MapSerializedSession(String id,
                                String schemeName,
                                Reason reason,
                                boolean warning,
                                Map<String, Object> properties) {
        this.id = id;
        this.schemeName = schemeName;
        this.reason = reason;
        this.warning = warning;
        this.properties = properties;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public Reason getReason() {
        return reason;
    }

    public boolean isWarning() {
        return warning;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
