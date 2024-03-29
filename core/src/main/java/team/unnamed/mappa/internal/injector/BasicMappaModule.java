package team.unnamed.mappa.internal.injector;

import team.unnamed.mappa.function.CollectionPropertyProvider;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.configuration.MultiNodeParseConfiguration;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.node.SchemeCollection;
import team.unnamed.mappa.model.map.property.*;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.DuplicateFlagException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.ParseUtils;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BasicMappaModule extends AbstractMappaModule {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

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
                .firstAlias(node.isFirstAlias())
                .readOnly(true)
                .build();
        });
        bindNode(Vector.class, (context, node) -> {
            AtomicBoolean yawPitch = new AtomicBoolean();
            AtomicBoolean noY = new AtomicBoolean();
            AtomicBoolean block = new AtomicBoolean();
            Function<Vector, Vector> processing = newVectorProvider(node.getArgs(),
                noY,
                yawPitch,
                block);
            MapNodeProperty.Builder<Vector> builder = MapNodeProperty
                .builder(node.getName(), Vector.class)
                .aliases(node.getAliases())
                .serializable(noY.get()
                    ? line -> Vector.fromStringNoY(line, yawPitch.get(), block.get())
                    : line -> Vector.fromString(line, yawPitch.get(), block.get()))
                .optional(node.isOptional())
                .firstAlias(node.isFirstAlias())
                .readOnly(true);
            if (processing != null) {
                builder.postProcessing(processing);
            }
            return builder.build();
        });
        bindNode(Cuboid.class, (context, node) -> {
            AtomicBoolean noY = new AtomicBoolean();
            Function<Vector, Vector> processing = newVectorProvider(node.getArgs(),
                noY,
                null,
                null);
            MapNodeProperty.Builder<Cuboid> builder = MapNodeProperty
                .builder(node.getName(), Cuboid.class)
                .aliases(node.getAliases())
                .serializableList(noY.get() ? Cuboid::fromStringsNoY : Cuboid::fromStrings)
                .optional(node.isOptional())
                .firstAlias(node.isFirstAlias())
                .readOnly(true);
            if (processing != null) {
                builder.postProcessing(cuboid -> new Cuboid(
                    processing.apply(cuboid.getMaximum()),
                    processing.apply(cuboid.getMinimum())
                ));
            }

            return builder.build();
        });
        bindNode(Chunk.class, (context, node) -> MapNodeProperty
            .builder(node.getName(), Chunk.class)
            .aliases(node.getAliases())
            .serializable(Chunk::fromString)
            .optional(node.isOptional())
            .firstAlias(node.isFirstAlias())
            .readOnly(true)
            .build());
        bindNode(ChunkCuboid.class,
            (context, node) -> MapNodeProperty
                .builder(node.getName(), ChunkCuboid.class)
                .aliases(node.getAliases())
                .serializableList(ChunkCuboid::fromStrings)
                .optional(node.isOptional())
                .firstAlias(node.isFirstAlias())
                .readOnly(true)
                .build());
        bindNode("metadata",
            String.class,
            (context, node) -> {
                Map<String, String> metadata = context.getObject(
                    ParseContext.METADATA,
                    id -> new LinkedHashMap<>());
                String[] args = node.getArgs();
                String absolutePath = context.getAbsolutePath();
                if (args.length == 0) {
                    throw new ParseException(
                        TranslationNode.METADATA_NO_NAME.with("{path}", absolutePath)
                    );
                }
                String name = args[0];
                metadata.put(name, absolutePath);
                MapNodeProperty.Builder<String> builder = MapNodeProperty
                    .builder(node.getName(), String.class)
                    .postProcessing(String::valueOf)
                    .optional(false)
                    .readOnly(true);

                // Remove name arg from args
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                ParseUtils.forEach(subArgs,
                    arg -> {
                        switch (arg) {
                            case "ignore":
                                builder.ignore(true);
                                builder.optional(true);
                                break;
                            case "session-id":
                                context.getObject(MapScheme.SESSION_ID_PATH,
                                    key -> absolutePath);
                                builder.immutableValueProvider(MapEditSession::getId);
                                context.getObject(MapScheme.IMMUTABLE_SET,
                                        key -> new LinkedHashSet<>())
                                    .add(absolutePath);
                                break;
                            case "creation-date":
                                builder.immutableValueProvider(
                                    session -> {
                                        Date now = new Date(System.currentTimeMillis());
                                        return DATE_FORMAT.format(now);
                                    });
                                context.getObject(MapScheme.IMMUTABLE_SET,
                                        key -> new LinkedHashSet<>())
                                    .add(absolutePath);
                                break;
                        }
                    });
                MapNodeProperty<String> build = builder
                    .build();
                // Very hardcoded :)
                return name.equals("author") ? new MapSetProperty(build) : build;
            });

        bindCollection(List.class, (context, collection, property) ->
            new MapListProperty(property));
        bindCollection(Set.class, (context, collection, property) ->
            new MapSetProperty(property));

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

            configMap.put(NodeParentParseConfiguration.PARENT_CONFIGURATION.getName(), parentConfig);
        });

        bindConfiguration(MultiNodeParseConfiguration.class, (context, config) -> {
            String currentPath = context.getCurrentPath();
            int lastDot = currentPath.lastIndexOf(".");
            String previousPath = currentPath.substring(0, lastDot);
            String pathToClone = currentPath.substring(lastDot + 1);
            @SuppressWarnings("unchecked")
            Map<String, Object> node = context.find(previousPath, Map.class);
            Map<String, Object> baseNode = (Map<String, Object>) node.get(pathToClone);
            MapPropertyTree tree = context.getTreeProperties();
            Set<String> subNodes = baseNode.keySet();
            subNodes.remove(MultiNodeParseConfiguration.NODE);

            Set<String> plain = context.getObject(MapScheme.PLAIN_KEYS, key -> new LinkedHashSet<>());
            List<String> multiNodes = config.getMultiNodes();
            for (String multiNode : multiNodes) {
                for (String pathNode : subNodes) {
                    if (pathNode.equals(config.getPath())) {
                        continue;
                    }

                    String propertyPath = currentPath + "." + pathNode;
                    MapProperty property = tree.find(propertyPath);
                    if (property == null) {
                        throw new ParseException(
                            TranslationNode
                                .CLONE_PATH_NOT_FOUND
                                .with("{path}", pathToClone));
                    }

                    String newPath = previousPath + "." + multiNode + "." + pathNode;

                    MapProperty clone;
                    MapNodeProperty<?> nodeProperty;
                    if (property instanceof MapCollectionProperty) {
                        MapCollectionProperty listProperty = (MapCollectionProperty) property;
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

                    if (property instanceof MapCollectionProperty) {
                        MapCollectionProperty collectionProperty = (MapCollectionProperty) property;

                        Type collectionType = collectionProperty.getCollectionType();
                        CollectionPropertyProvider provider = injector.getFactoryCollection(collectionType);
                        SchemeCollection schemeCollection = context.find(newPath, SchemeCollection.class);
                        clone = provider.parse(context, schemeCollection, nodeProperty);
                    }

                    context.putProperty(newPath, clone);
                    plain.add(newPath);
                }
            }

            for (String subNode : subNodes) {
                String path = currentPath + "." + subNode;
                plain.remove(path);
                context.remove(path);
            }
            node.remove(pathToClone);
            context.remove(currentPath);
            tree.clearAll(currentPath);
        });
    }

    protected Function<Vector, Vector> newVectorProvider(String[] args,
                                                         AtomicBoolean noY,
                                                         AtomicBoolean yawPitch,
                                                         AtomicBoolean block)
        throws ParseException {
        ParseUtils.forEach(args, arg -> {
            if (arg.equals("yaw-pitch")) {
                if (yawPitch == null) {
                    return;
                }

                if (yawPitch.get()) {
                    throw new DuplicateFlagException(
                        TranslationNode.FLAG_DUPLICATION.with("{key}", "no-yaw-pitch"));
                }
                yawPitch.set(true);
            }
            if (arg.equals("no-y")) {
                if (noY.get()) {
                    throw new DuplicateFlagException(
                        TranslationNode.FLAG_DUPLICATION.with("{key}", "no-y"));
                }
                noY.set(true);
            }

            if (arg.equals("block")) {
                if (block == null) {
                    return;
                }

                if (block.get()) {
                    throw new DuplicateFlagException(
                        TranslationNode.FLAG_DUPLICATION.with("{key}", "block"));
                }
                block.set(true);
            }
        });
        Function<Vector, Vector> processing = null;
        if (yawPitch != null && yawPitch.get()) {
            processing = vector -> vector.setYawPitch(true);
        }
        if (block != null && block.get()) {
            processing = vector -> vector.setBlock(true);
        }
        if (noY.get()) {
            Function<Vector, Vector> mutNoYaw = vector -> vector.mutNoY(true);
            if (processing != null) {
                processing = processing.andThen(mutNoYaw);
            } else {
                processing = mutNoYaw;
            }
        }
        return processing;
    }
}
