package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.scheme.MapScheme;

public interface MapSession {

    void setId(String id);

    void setWarning(boolean b);

    boolean containsProperty(String property);

    String getId();

    String getSchemeName();

    MapScheme getScheme();

    boolean isWarning();

}
