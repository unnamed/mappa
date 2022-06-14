package team.unnamed.mappa.internal.injector;

import team.unnamed.mappa.model.map.configuration.MultiNodeParseConfiguration;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.property.MapListProperty;
import team.unnamed.mappa.model.map.property.MapNodeProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.DuplicateFlagException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.ParseUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BasicMappaModule extends AbstractMappaModule {

    @SuppressWarnings("unchecked")
    @Override
    public void configure() {
        bindNode(Boolean.class, (context, node) ->
            MapNodeProperty.builder(node.getName(), Boolean.class)
                .optional(node.isOptional())
                .readOnly(true)
                .build());
        bindNode(Integer.class, (context, node) -> {
            Condition.Builder<Integer> builder = Condition.builder();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, i -> i > -1, TranslationNode.NUMBER_NON_POSITIVE.with("{number}", arg))
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, i -> i < 1, TranslationNode.NUMBER_NON_NEGATIVE.with("{number}", arg))
                        .block("+");
                }
            });

            return MapNodeProperty.builder(node.getName(), Integer.class)
                .condition(builder.build())
                .optional(node.isOptional())
                .readOnly(true)
                .build();
        });
        bindNode(Long.class, (context, node) -> {
            Condition.Builder<Long> builder = Condition.builder();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, l -> l > -1, TranslationNode.NUMBER_NON_POSITIVE.with("{number}", arg))
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, l -> l < 1, TranslationNode.NUMBER_NON_NEGATIVE.with("{number}", arg))
                        .block("+");
                }
            });
            return MapNodeProperty.builder(node.getName(), Long.class)
                .condition(builder.build())
                .optional(node.isOptional())
                .readOnly(true)
                .build();
        });
        bindNode(Double.class, (context, node) -> {
            Condition.Builder<Double> builder = Condition.builder();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, d -> d > -1, TranslationNode.NUMBER_NON_POSITIVE.with("{number}", arg))
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, d -> d < 1, TranslationNode.NUMBER_NON_NEGATIVE.with("{number}", arg))
                        .block("+");
                }
            });
            return MapNodeProperty.builder(node.getName(), Double.class)
                .condition(builder.build())
                .optional(node.isOptional())
                .readOnly(true)
                .build();
        });
        bindNode(Float.class, (context, node) -> {
            Condition.Builder<Float> builder = Condition.builder();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("+")) {
                    builder
                        .filter(arg, f -> f > -1, TranslationNode.NUMBER_NON_POSITIVE.with("{number}", arg))
                        .block("-");
                } else if (arg.equals("-")) {
                    builder
                        .filter(arg, f -> f < 1, TranslationNode.NUMBER_NON_NEGATIVE.with("{number}", arg))
                        .block("+");
                }
            });

            return MapNodeProperty.builder(node.getName(), Float.class)
                .condition(builder.build())
                .optional(node.isOptional())
                .readOnly(true)
                .build();
        });
        bindNode(String.class, (context, node) -> {
            AtomicReference<Function<String, String>> function = new AtomicReference<>();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("lower-case")) {
                    if (function.get() != null) {
                        throw new DuplicateFlagException(
                            TranslationNode.FLAG_CONFLICT.with(
                                "{key}", "lower-case",
                                "{conflict}", "upper-case")
                        );
                    }
                    function.set(String::toUpperCase);
                } else if (arg.equals("upper-case")) {
                    if (function.get() != null) {
                        throw new DuplicateFlagException(
                            TranslationNode.FLAG_CONFLICT.with(
                                "{key}", "upper-case",
                                "{conflict}", "lower-case")
                        );
                    }

                    function.set(String::toUpperCase);
                }
            });
            return MapNodeProperty.builder(node.getName(), String.class)
                .aliases(node.getAliases())
                .postProcessing(function.get())
                .optional(node.isOptional())
                .readOnly(true)
                .build();
        });
        bindNode(Vector.class, (context, node) -> {
            AtomicBoolean noYawPitch = new AtomicBoolean();
            AtomicBoolean noY = new AtomicBoolean();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("no-yaw-pitch")) {
                    if (noYawPitch.get()) {
                        throw new DuplicateFlagException(
                            TranslationNode.FLAG_DUPLICATION.with("{key}", "no-yaw-pitch"));
                    }
                    noYawPitch.set(true);
                }
                if (arg.equals("no-y")) {
                    if (noY.get()) {
                        throw new DuplicateFlagException(
                            TranslationNode.FLAG_DUPLICATION.with("{key}", "no-y"));
                    }
                    noY.set(true);
                }
            });
            MapNodeProperty.Builder<Vector> builder = MapNodeProperty
                .builder(node.getName(), Vector.class)
                .aliases(node.getAliases())
                .serializable(noY.get() ?  Vector::fromStringNoY : Vector::fromString)
                .optional(node.isOptional())
                .readOnly(true);
            Function<Vector, Vector> processing = null;
            if (noYawPitch.get()) {
                processing = Vector::removeYawPitch;
            }
            if (noY.get()) {
                Function<Vector, Vector> mutNoYaw = vector -> vector.mutNoY(true);
                if (processing != null) {
                    processing = processing.andThen(mutNoYaw);
                } else {
                    processing = mutNoYaw;
                }
            }
            if (processing != null) {
                builder.postProcessing(processing);
            }
            return builder.build();
        });
        bindNode(Cuboid.class, (context, node) -> {
            AtomicBoolean noY = new AtomicBoolean();
            AtomicBoolean noYawPitch = new AtomicBoolean();
            ParseUtils.forEach(node.getArgs(), arg -> {
                if (arg.equals("no-yaw-pitch")) {
                    if (noYawPitch.get()) {
                        throw new DuplicateFlagException(
                            TranslationNode.FLAG_DUPLICATION.with("{key}", "no-yaw-pitch"));
                    }
                    noYawPitch.set(true);
                }
                if (arg.equals("no-y")) {
                    if (noY.get()) {
                        throw new DuplicateFlagException(
                            TranslationNode.FLAG_DUPLICATION.with("{key}", "no-y"));
                    }
                    noY.set(true);
                }
            });
            MapNodeProperty.Builder<Cuboid> builder = MapNodeProperty
                .builder(node.getName(), Cuboid.class)
                .aliases(node.getAliases())
                .serializableList(noY.get() ? Cuboid::fromStringsNoY : Cuboid::fromStrings)
                .optional(node.isOptional())
                .readOnly(true);
            Function<Vector, Vector> processing = null;
            if (noYawPitch.get()) {
                processing = Vector::removeYawPitch;
            }
            if (noY.get()) {
                Function<Vector, Vector> mutNoYaw = vector -> vector.mutNoY(true);
                if (processing != null) {
                    processing = processing.andThen(mutNoYaw);
                } else {
                    processing = mutNoYaw;
                }
            }
            if (processing != null) {
                Function<Vector, Vector> finalProcessing = processing;
                builder.postProcessing(cuboid -> new Cuboid(
                    finalProcessing.apply(cuboid.getMaximum()),
                    finalProcessing.apply(cuboid.getMinimum())
                ));
            }
            return builder.build();
        });
        bindNode(Chunk.class, (context, node) -> MapNodeProperty
            .builder(node.getName(), Chunk.class)
            .aliases(node.getAliases())
            .serializable(Chunk::fromString)
            .optional(node.isOptional())
            .readOnly(true)
            .build());
        bindNode(ChunkCuboid.class,
            (context, node) -> MapNodeProperty
                .builder(node.getName(), ChunkCuboid.class)
                .aliases(node.getAliases())
                .serializableList(ChunkCuboid::fromStrings)
                .optional(node.isOptional())
                .readOnly(true)
                .build());
        bindNode("property",
            String.class,
            (context, node) -> {
                Map<String, Object> configuration = context.getParseConfiguration();
                Map<String, String> buildProperties = (Map<String, String>) configuration.computeIfAbsent(
                    ParseContext.BUILD_PROPERTIES,
                    id -> new LinkedHashMap<String, String>());
                String[] args = node.getArgs();
                if (args.length == 0) {
                    throw new ParseException(
                        TranslationNode.BUILD_PROPERTY_NOT_NAME.with("{path}", context.getAbsolutePath())
                    );
                }
                String name = args[0];
                buildProperties.put(name, context.getAbsolutePath());
                MapNodeProperty<String> build = MapNodeProperty
                    .builder(node.getName(), String.class)
                    .optional(false)
                    .readOnly(true)
                    .build();
                // Very hardcoded :)
                return name.equals("author") ? new MapListProperty(build) : build;
            });

        bindCollection(List.class, (context, collection, property) ->
            new MapListProperty(property));

        bindConfiguration(NodeParentParseConfiguration.class, (context, config) -> {
            Map<String, Object> configMap = context.getParseConfiguration();
            Map<String, Object> parentConfig = new HashMap<>();
            String formatName = config.getFormatName();
            if (formatName != null) {
                parentConfig.put("format-name", formatName);
            }
            String[] aliases = config.getAliases();
            if (aliases != null) {
                parentConfig.put("aliases", aliases);
            }

            configMap.put(NodeParentParseConfiguration.PARENT_CONFIGURATION, parentConfig);
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
            Set<String> subNodes = baseNode.keySet();
            subNodes.remove(MultiNodeParseConfiguration.NODE);

            List<String> multiNodes = config.getMultiNodes();
            for (String multiNode : multiNodes) {
                for (String pathNode : subNodes) {
                    if (pathNode.equals(config.getPath())) {
                        continue;
                    }

                    String propertyPath = currentPath + "." + pathNode;
                    MapProperty property = properties.get(propertyPath);
                    if (property == null) {
                        throw new ParseException(
                            TranslationNode.CLONE_PATH_NOT_FOUND.with("{path}", pathToClone));
                    }

                    String nodePath = previousPath + "." + multiNode + "." + pathNode;

                    MapProperty clone;
                    MapNodeProperty<?> nodeProperty;
                    if (property instanceof MapListProperty) {
                        MapListProperty listProperty = (MapListProperty) property;
                        nodeProperty = listProperty.getDelegate();
                    } else {
                        nodeProperty = (MapNodeProperty<?>) property;
                    }

                    clone = nodeProperty.toBuilder()
                        .postVerification(session -> {
                            int nullNodes = 0;
                            for (String cloneNode : multiNodes) {
                                Boolean set = null;
                                for (String subNode : subNodes) {
                                    String subNodePath = previousPath + "." + cloneNode + "." + subNode;
                                    boolean contains = session.containsProperty(subNodePath);
                                    if (set == null) {
                                        set = contains;
                                        continue;
                                    }

                                    if (set != contains) {
                                        return TranslationNode
                                            .UNDEFINED_PROPERTY
                                            .withFormal("{property}", subNodePath);
                                    }
                                }
                                if (Boolean.FALSE.equals(set)) {
                                    ++nullNodes;
                                }
                            }
                            return nullNodes == multiNodes.size()
                                ? TranslationNode
                                .UNDEFINED_PROPERTY
                                .withFormal("{property}", previousPath + ".*." + pathNode)
                                : null;
                        })
                        .build();

                    if (property instanceof MapListProperty) {
                        clone = new MapListProperty(nodeProperty);
                    }
                    properties.put(nodePath, clone);
                }
            }

            subNodes.forEach(subNode -> properties.remove(currentPath + "." + subNode));
        });
    }
}
