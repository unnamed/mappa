package team.unnamed.mappa.yaml.constructor;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;
import java.util.Objects;

public class SessionConstructor extends PlainConstructor {
    public static final String SESSION_KEY = "session!!";

    private final Object sender;
    private final MappaBootstrap bootstrap;
    private final boolean loadWarning;

    public SessionConstructor(Object sender,
                              MappaBootstrap bootstrap,
                              boolean loadWarning) {
        this.sender = sender;
        this.bootstrap = bootstrap;
        this.loadWarning = loadWarning;
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

            MapScheme mapScheme = bootstrap.getScheme(key);
            if (mapScheme == null) {
                throw new IllegalArgumentException(
                    "Map scheme " + key + " not found while trying to resume sessions.");
            }

            String id = (String) Objects.requireNonNull(construct.get("id"));
            Boolean wrapper = (Boolean) construct.get("warning");
            boolean warning = wrapper != null && wrapper;
            Map<String, Object> properties = (Map<String, Object>) construct.get("properties");


            MapSerializedSession.Reason reason = null;
            if (bootstrap.containsSessionID(id)) {
                reason = MapSerializedSession.Reason.DUPLICATE;
                id = bootstrap.generateStringID(id);
            } else if (warning && !loadWarning) {
                reason = MapSerializedSession.Reason.WARNING;
            }

            if (reason != null) {
                return bootstrap.newMapSerializedSession(id,
                    mapScheme,
                    reason,
                    warning,
                    properties);
            }

            try {
                Map<String, Object> plainMap = plainMap(properties);
                MapEditSession session = bootstrap.resumeSession(
                    sender, id, mapScheme, plainMap);
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
