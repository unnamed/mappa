package team.unnamed.mappa.yaml;

import org.yaml.snakeyaml.nodes.Node;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;

import java.util.Map;

public interface MapParseConfigurationFunction {

    NodeParseConfiguration apply(Node node, Map<String, Object> map);
}
