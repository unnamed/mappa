package team.unnamed.mappa.model.map.injector;

import team.unnamed.mappa.model.map.configuration.MultiNodeParseConfiguration;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.property.MapListProperty;
import team.unnamed.mappa.model.map.property.MapNodeProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.DuplicateFlagException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.ParseUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BasicModule extends AbstractMappaModule {

    @SuppressWarnings("unchecked")
    @Override
    public void configure() {
        bindNode(Boolean.class, (context, node) ->
            MapNodeProperty.builder(node.getName())
                .conditionOfType(Boolean.class)
                .optional(node.isOptional())
                .build());
        bindNode(Integer.class, (context, node) -> {
            Condition.Builder<Integer> builder = Condition.builder(int.class);
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, i -> i > -1, "parse.error.int-non-positive")
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, i -> i < 1, "parse.error.int-non-negative")
                        .block("+");
                }
            });

            return MapNodeProperty.builder(node.getName())
                .condition(builder.build())
                .optional(node.isOptional())
                .build();
        });
        bindNode(Long.class, (context, node) -> {
            Condition.Builder<Long> builder = Condition.builder(long.class);
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, l -> l > -1, "parse.error.int-non-positive")
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, l -> l < 1, "parse.error.int-non-negative")
                        .block("+");
                }
            });
            return MapNodeProperty.builder(node.getName())
                .condition(builder.build())
                .optional(node.isOptional())
                .build();
        });
        bindNode(Double.class, (context, node) -> {
            Condition.Builder<Double> builder = Condition.builder(double.class);
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, d -> d > -1, "parse.error.int-non-positive")
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, d -> d < 1, "parse.error.int-non-negative")
                        .block("+");
                }
            });
            return MapNodeProperty.builder(node.getName())
                .condition(builder.build())
                .optional(node.isOptional())
                .build();
        });
        bindNode(Float.class, (context, node) -> {
            Condition.Builder<Float> builder = Condition.builder(float.class);
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, f -> f > -1, "parse.error.int-non-positive")
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, f -> f < 1, "parse.error.int-non-negative")
                        .block("+");
                }
            });

            return MapNodeProperty.builder(node.getName())
                .condition(builder.build())
                .optional(node.isOptional())
                .build();
        });
        bindNode(String.class, (context, node) -> {
            Condition condition = Condition.ofType(String.class);
            AtomicReference<Function<String, String>> function = new AtomicReference<>();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("lower-case")) {
                    if (function.get() != null) {
                        throw new DuplicateFlagException(
                            String.format("Flag key %s conflicts with %s or already exists", "lower-case", "upper-case")
                        );
                    }
                    function.set(String::toUpperCase);
                } else if (arg.equals("upper-case")) {
                    if (function.get() != null) {
                        throw new DuplicateFlagException(
                            String.format("Flag key %s conflicts with %s or already exists", "upper-case", "lower-case")
                        );
                    }

                    function.set(String::toUpperCase);
                }
            });
            return MapNodeProperty.builder(node.getName())
                .condition(condition)
                .postProcessing(function.get())
                .optional(node.isOptional())
                .build();
        });
        bindNode(Vector.class, (context, node) -> {
            AtomicBoolean onlyAxis = new AtomicBoolean();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("only-axis")) {
                    if (onlyAxis.get()) {
                        throw new DuplicateFlagException("Flag key only-axis is already set!");
                    }
                    onlyAxis.set(true);
                }
            });
            Function<Vector, Vector> postProcessing = onlyAxis.get()
                ? Vector::removeYawPitch
                : null;
            return MapNodeProperty.builder(node.getName())
                .conditionOfType(Vector.class)
                .postProcessing(postProcessing)
                .optional(node.isOptional())
                .build();
        });
        bindNode(Cuboid.class, (context, node) ->
            MapNodeProperty.builder(node.getName())
                .conditionOfType(Cuboid.class)
                .optional(node.isOptional())
                .build());
        bindNode(Chunk.class, (context, node) ->
            MapNodeProperty.builder(node.getName())
                .conditionOfType(Chunk.class)
                .optional(node.isOptional())
                .build());
        bindNode(ChunkCuboid.class,
            (context, node) -> MapNodeProperty.builder(node.getName())
                .conditionOfType(ChunkCuboid.class)
                .optional(node.isOptional())
                .build());
        bindNode("property",
            String.class,
            (context, node) -> MapNodeProperty.builder(node.getName())
                .conditionOfType(String.class)
                .optional(false)
                .buildProperty(true)
                .build());

        bindCollection(List.class, (context, collection, property) ->
                new MapListProperty(property));

        bindConfiguration(NodeParentParseConfiguration.class, (context, config) -> {
            Map<String, Object> configMap = context.getParseConfiguration();
            configMap.put("interpret", config.getMode());
            configMap.put("format-name", config.getFormatName());
            String[] aliases = config.getAliases();
            if (aliases != null) {
                configMap.put("aliases", aliases);
            }
        });

        bindConfiguration(MultiNodeParseConfiguration.class, (context, config) -> {
            String currentPath = context.getCurrentPath();
            int lastDot = currentPath.lastIndexOf(".");
            String previousPath = currentPath.substring(0, lastDot);
            String pathToClone = currentPath.substring(lastDot + 1);
            @SuppressWarnings("unchecked")
            Map<String, Object> node = context.find(previousPath, Map.class);
            Map<String, Object> baseNode = (Map<String, Object>) node.get(pathToClone);
            Map<String, MapProperty> properties = context.getProperties();
            for (String path : baseNode.keySet()) {
                if (path.equals(config.getPath())) {
                    continue;
                }
                String propertyPath = previousPath + "." + pathToClone + "." + path;
                MapProperty property = properties.get(propertyPath);
                if (property == null) {
                    throw new ParseException("Trying to clone property at path " + propertyPath + ", found null");
                }

                for (String multiNode : config.getMultiNodes()) {
                    String nodePath = previousPath + "." + multiNode + "." + path;
                    properties.put(nodePath, property.clone());
                }

                properties.remove(propertyPath);
            }
        });
    }
}
