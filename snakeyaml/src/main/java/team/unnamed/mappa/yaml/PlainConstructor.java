package team.unnamed.mappa.yaml;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlainConstructor extends SafeConstructor {

    public PlainConstructor(String prefix) {
        this.yamlConstructors.put(Tag.MAP, new ConstructPlainMap(prefix));
    }

    public class ConstructPlainMap extends SafeConstructor.ConstructYamlMap {
        private final String prefix;
        private boolean first = true;

        public ConstructPlainMap(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Object construct(Node node) {
            Map<String, Object> map = (Map<String, Object>) super.construct(node);
            Map<String, Object> plainMap = new LinkedHashMap<>();
            plainMap(first ? prefix : "", map, plainMap);
            return plainMap;
        }

        public void plainMap(String path,
                             Map<String, Object> map,
                             Map<String, Object> toWrite) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String absolutePath = path.isEmpty() ? key : path + "." + key;
                if (first) {
                    first = false;
                }
                if (value instanceof Map) {
                    Map<String, Object> subMap = (Map<String, Object>) value;
                    plainMap(absolutePath, subMap, toWrite);
                    continue;
                }

                toWrite.put(absolutePath, value);
            }
        }
    }
}
