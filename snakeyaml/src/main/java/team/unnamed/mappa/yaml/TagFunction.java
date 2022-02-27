package team.unnamed.mappa.yaml;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.nodes.Node;

public interface TagFunction {

    @Nullable
    Object apply(Node node, String args);
}
