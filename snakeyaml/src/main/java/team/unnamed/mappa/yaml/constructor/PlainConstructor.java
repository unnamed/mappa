package team.unnamed.mappa.yaml.constructor;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;

public class PlainConstructor extends SafeConstructor {

    public PlainConstructor() {
        this.yamlConstructors.put(Tag.MAP, new ConstructNodePlainMap());
    }

    public abstract class ConstructPlainMap extends SafeConstructor.ConstructYamlMap {

        protected Map<String, Object> plainMap(Map<String, Object> map) {
            Map<String, Object> plainMap = new LinkedHashMap<>();
            plainMap("", map, plainMap);
            return plainMap;
        }

        protected void plainMap(String path,
                                Map<String, Object> map,
                                Map<String, Object> toWrite) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String absolutePath = path;
                if (!absolutePath.isEmpty()) {
                    absolutePath += "." + key;
                } else {
                    absolutePath = key;
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

    public class ConstructNodePlainMap extends ConstructPlainMap {
        private boolean first = true;

        private final Set<String> parentNodes = new HashSet<>();

        @Override
        public Object construct(Node node) {
            if (first) {
                MappingNode mappingNode = (MappingNode) node;
                List<NodeTuple> firstNodes = mappingNode.getValue();
                for (NodeTuple nodeTuple : firstNodes) {
                    ScalarNode scalar = (ScalarNode) nodeTuple.getKeyNode();
                    parentNodes.add(scalar.getValue());
                }

                first = false;
            }

            Map<String, Object> map = (Map<String, Object>) super.construct(node);
            boolean areParents = true;
            for (String key : map.keySet()) {
                if (!parentNodes.contains(key)) {
                    areParents = false;
                    break;
                }
            }

            if (areParents) {
                return map;
            }

            return plainMap(map);
        }
    }
}
