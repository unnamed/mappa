package team.unnamed.mappa.yaml.constructor;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SessionConstructor extends PlainConstructor {
    public static final String SESSION_KEY = "session!!";

    private final Map<String, MapScheme> schemeMap;
    private final boolean loadWarning;
    private final Set<String> blackList;

    public SessionConstructor(Map<String, MapScheme> schemeMap,
                              boolean loadWarning,
                              Set<String> blackList) {
        this.schemeMap = schemeMap;
        this.loadWarning = loadWarning;
        this.blackList = blackList;
        this.yamlConstructors.put(Tag.MAP, new ConstructSession());
    }

    public class ConstructSession extends PlainConstructor.ConstructPlainMap {

        @SuppressWarnings("unchecked")
        @Override
        public Object construct(Node node) {
            Map<String, Object> construct =
                (Map<String, Object>) super.construct(node);
            String key = (String) construct.get(SESSION_KEY);
            if (key == null) {
                return construct;
            }

            MapScheme mapScheme = schemeMap.get(key);
            if (mapScheme == null) {
                return null;
            }

            String id = (String) Objects.requireNonNull(construct.get("id"));
            Boolean wrapper = (Boolean) construct.get("warning");
            boolean warning = wrapper != null && wrapper;
            Map<String, Object> properties = (Map<String, Object>) construct.get("properties");
            if (blackList.contains(id)) {
                return new MapSerializedSession(id,
                    mapScheme.getName(),
                    MapSerializedSession.Reason.DUPLICATE,
                    warning,
                    properties);
            } else if (warning && !loadWarning) {
                return new MapSerializedSession(id,
                    mapScheme.getName(),
                    MapSerializedSession.Reason.WARNING,
                    true,
                    properties);
            }

            try {
                MapEditSession session = mapScheme.resumeSession(id, plainMap(properties));
                if (warning) {
                    session.setWarning(true);
                }
                return session;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
