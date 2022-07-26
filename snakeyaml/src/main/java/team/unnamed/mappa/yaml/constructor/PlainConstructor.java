package team.unnamed.mappa.yaml.constructor;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;
import team.unnamed.mappa.internal.mapper.SchemeMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlainConstructor extends SafeConstructor {

    public PlainConstructor() {
        this.yamlConstructors.put(Tag.MAP, new ConstructNodePlainMap());
    }

    public abstract class ConstructPlainMap extends SafeConstructor.ConstructYamlMap {

        protected Map<String, Object> plainMap(Map<String, Object> map) {
            return SchemeMapper.plainMap(map);
        }

        protected void plainMap(String path,
                                Map<String, Object> map,
                                Map<String, Object> toWrite) {
            SchemeMapper.plainMap(path, map, toWrite);
        }
    }

    public class ConstructNodePlainMap extends ConstructPlainMap {
        private boolean first = true;

        private final Set<String> parentNodes = new HashSet<>();

        @SuppressWarnings("unchecked")
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
