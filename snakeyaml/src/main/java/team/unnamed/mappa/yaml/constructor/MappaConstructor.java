package team.unnamed.mappa.yaml.constructor;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import team.unnamed.mappa.model.map.configuration.MultiNodeParseConfiguration;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;
import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.model.region.Cuboid;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.TypeUtils;
import team.unnamed.mappa.yaml.function.MapParseConfigurationFunction;
import team.unnamed.mappa.yaml.function.TagFunction;

import java.util.*;

public class MappaConstructor extends SafeConstructor {
    public static final String PROPERTY_KEY = "$";
    public static final String ALIASES_KEY = "~";
    public static final int LINE_SEPARATOR = 10;

    private final Map<String, TagFunction> tags = new HashMap<>();
    private final Map<String, MapParseConfigurationFunction> parseConfigurationMap = new HashMap<>();

    private String buffer;

    /**
     * Get name of node from the start mark buffer.
     *
     * @param node Node to get name.
     * @return Name of node.
     */
    public String getNameOfNode(Node node) {
        Mark startMark = node.getStartMark();
        StringBuilder builder = new StringBuilder();
        boolean map = node instanceof MappingNode;
        int index = startMark.getIndex();
        if (!map) {
            index -= 2;
        }
        while (index != 0) {
            int codePoint = buffer.charAt(map ? ++index : --index);
            if (Character.isSpaceChar(codePoint)) {
                break;
            }
            builder.appendCodePoint(codePoint);
        }
        if (!map) {
            builder.reverse();
        }
        return builder.toString();
    }

    /**
     * Not efficient.
     */
    @Deprecated
    public String getAdjacentComment(Node node) {
        Mark startMark = node.getStartMark();
        boolean starts = false;
        boolean fails = false;
        StringBuilder builder = new StringBuilder();
        int pointer = startMark.getIndex() - 3; // Jump to node name
        while (!fails) {
            int codePoint = buffer.charAt(pointer--);
            if (codePoint == '#') {
                break;
            }

            char[] chars = Character.toChars(codePoint);
            if (starts) {
                builder.append(chars);
            }

            if (codePoint == LINE_SEPARATOR) {
                if (starts) {
                    fails = true;
                } else {
                    starts = true;
                }
            }
        }
        return fails ? null : builder.toString();
    }

    public MappaConstructor() {
        registerTag("list", (node, args) ->
            newNodeCollectionFrom(node, args, List.class));
        registerTag("set", (node, args) ->
            newNodeCollectionFrom(node, args, Set.class));

        // No args needs to be parsed in boolean
        registerTag("boolean", (node, args) ->
            newNodeFrom(node, Boolean.class, null, new String[0]));
        registerTagPrimitive(int.class);
        registerTagPrimitive(long.class);
        registerTagPrimitive(double.class);
        registerTagPrimitive(float.class);
        registerTagPrimitive(char.class);
        registerTagGeneric(String.class);
        registerTagGeneric(Vector.class);
        registerTagGeneric(Cuboid.class);
        registerTagGeneric(Chunk.class);
        registerTagGeneric("chunk-cuboid", ChunkCuboid.class);
        registerWithTagGeneric("property", String.class, false);

        registerProperty(NodeParentParseConfiguration.NODE, (node, map) -> {
            String formatName = (String) map.get("format-name");
            String path = (String) map.get("path");
            Object objectAlias = map.get("aliases");
            String[] aliases = null;
            if (objectAlias instanceof String) {
                aliases = new String[]{(String) objectAlias};
            } else if (objectAlias instanceof String[]) {
                aliases = (String[]) objectAlias;
            } else if (objectAlias instanceof List) {
                List<?> list = (List<?>) objectAlias;
                aliases = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    aliases[i] = String.valueOf(list.get(i));
                }
            }

            return new NodeParentParseConfiguration(formatName, path, aliases);
        });
        registerProperty(MultiNodeParseConfiguration.NODE, (node, map) ->
            new MultiNodeParseConfiguration((List<String>) map.get("value")));

        this.yamlConstructors.put(Tag.MAP, new ConstructMappaProperty(parseConfigurationMap));
    }

    private String[] newSubArray(String[] array, int start) {
        String[] subArray = new String[array.length - start];
        int slot = 0;
        for (int i = start; i < array.length; i++) {
            subArray[slot++] = array[start++];
        }
        return subArray;
    }

    private SchemeNode newNodeFrom(Node node, Class<?> clazz, String tag, String[] args) {
        String nameOfNode = getNameOfNode(node);
        return newNodeFrom(nameOfNode, clazz, tag, args);
    }

    private SchemeNode newNodeFrom(String name, Class<?> clazz, String tag, String... args) {
        return newNodeFrom(name, clazz, tag, isNameOptional(name), args);
    }

    private SchemeNode newNodeFrom(Node node, Class<?> clazz, String tag, boolean optional, String[] args) {
        return newNodeFrom(getNameOfNode(node), clazz, tag, optional, args);
    }

    private SchemeNode newNodeFrom(String name, Class<?> clazz, String tag, boolean optional, String[] args) {
        String[] aliases;
        boolean firstAlias = false;
        if (args != null) {
            List<String> listAlias = new ArrayList<>();
            int start = -1;
            int end = -1;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (start == -1) {
                    if (!arg.startsWith(ALIASES_KEY)) {
                        continue;
                    }

                    char firstChar = arg.charAt(1);
                    if (firstChar == '{') {
                        start = i;

                        int lastIndex = arg.length() - 1;
                        char lastChar = arg.charAt(lastIndex);
                        if (lastChar == ',') {
                            arg = arg.substring(0, lastIndex);
                        }

                        listAlias.add(arg.substring(2));
                    } else {
                        listAlias.add(arg.substring(1));
                        break;
                    }
                } else {
                    int lastIndex = arg.length() - 1;
                    char lastChar = arg.charAt(lastIndex);
                    if (lastChar == '}') {
                        end = i;
                        listAlias.add(arg.substring(0, lastIndex));
                        break;
                    } else {
                        if (lastChar == ',') {
                            arg = arg.substring(0, lastIndex);
                        }
                        listAlias.add(arg);
                    }
                }
            }

            if (start != -1 && end == -1) {
                throw new IllegalArgumentException("Aliases bad syntax: " + Arrays.toString(args));
            }

            if (listAlias.isEmpty()) {
                aliases = null;
            } else {
                if (start == -1) {
                    aliases = new String[]{listAlias.get(0)};
                    args = new String[0];
                } else {
                    aliases = listAlias.toArray(new String[0]);
                    if (aliases.length == args.length) {
                        args = new String[0];
                    } else {
                        String[] newArgs = new String[args.length - aliases.length];
                        int index = 0;
                        for (int i = 0; i < args.length; i++) {
                            if (i >= start && i <= end) {
                                continue;
                            }
                            newArgs[index++] = args[i];
                        }
                        args = newArgs;
                    }
                }

                int remove = -1;
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    if (arg.equals("~~first")) {
                        remove = i;
                        break;
                    }
                }

                if (remove != -1) {
                    firstAlias = true;
                    args = rebuildWithout(remove, args);
                }
            }
        } else {
            aliases = null;
        }
        return SchemeNode.builder()
            .name(name)
            .type(clazz)
            .tag(tag)
            .optional(optional, true)
            .args(args)
            .aliases(aliases)
            .firstAlias(firstAlias)
            .build();
    }

    public String[] rebuildWithout(int index, String[] array) {
        String[] strings = new String[array.length - 1];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = array[i >= index ? i + 1 : i];
        }
        return strings;
    }

    private boolean isNameOptional(String name) {
        return name.charAt(name.length() - 1) == '?';
    }

    public void registerTagGeneric(Class<?> clazz) {
        registerTagGeneric(clazz.getSimpleName().toLowerCase(), clazz);
    }

    public void registerTagPrimitive(Class<?> clazz) {
        registerTagGeneric(clazz.getSimpleName().toLowerCase(), TypeUtils.primitiveToWrapper(clazz));
    }

    public void registerTagGeneric(Class<?> clazz, boolean optional) {
        registerTagGeneric(clazz.getSimpleName().toLowerCase(), clazz, optional);
    }

    public void registerTagGeneric(String tag, Class<?> clazz) {
        registerTag(tag, (node, args) -> newNodeFrom(node, clazz, null, args));
    }

    public void registerTagGeneric(String tag, Class<?> clazz, boolean optional) {
        registerTag(tag, (node, args) -> newNodeFrom(node, clazz, null, optional, args));
    }

    public void registerWithTagGeneric(String tag, Class<?> clazz, boolean optional) {
        registerTag(tag, (node, args) -> {
            String nameOfNode = getNameOfNode(node);
            return newNodeFrom(nameOfNode, clazz, tag, optional, args);
        });
    }

    public void registerTag(String tag, TagFunction function) {
        this.yamlConstructors.put(new Tag("!" + tag), new ConstructStringTag(function));
        this.tags.put(tag, function);
    }

    public void registerProperty(String property, MapParseConfigurationFunction function) {
        this.parseConfigurationMap.put(property, function);
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    @SuppressWarnings("rawtypes")
    private Object newNodeCollectionFrom(Node node, String[] args, Class<? extends Collection> clazz) {
        SchemeNode typeNode;
        String nodeName = getNameOfNode(node);
        if (args == null || args.length == 0) {
            typeNode = newNodeFrom(nodeName, Object.class, null);
        } else {
            if (args.length < 2) {
                throw new IllegalArgumentException(
                    "Incomplete sentence for list type: " + Arrays.toString(args));
            }
            String tagName = args[1];
            TagFunction function = tags.get(tagName);
            String[] subArgs = args.length > 2
                ? newSubArray(args, 2)
                : new String[0];
            Object result = function.apply(node, subArgs);
            if (result instanceof SchemeNode) {
                typeNode = (SchemeNode) result;
            } else {
                String name = nodeName + "." + tagName;
                typeNode = newNodeFrom(name, Object.class, tagName);
            }
        }

        return SchemeNode.newCollection(nodeName, clazz, typeNode);
    }

    public class ConstructStringTag extends SafeConstructor.ConstructYamlStr {
        private final TagFunction toEntity;

        public ConstructStringTag(TagFunction toEntity) {
            this.toEntity = toEntity;
        }

        @Override
        public Object construct(Node node) {
            String construct = (String) super.construct(node);
            return toEntity.apply(node,
                construct == null || construct.isEmpty()
                    ? new String[0]
                    : construct.split(" "));
        }
    }

    public class ConstructMappaProperty extends SafeConstructor.ConstructYamlMap {
        private final Map<String, MapParseConfigurationFunction> parseMap;

        public ConstructMappaProperty(Map<String, MapParseConfigurationFunction> parseMap) {
            this.parseMap = parseMap;
        }

        @Override
        public Map<String, Object> construct(Node node) {
            Map<String, Object> construct = (Map<String, Object>) super.construct(node);
            NodeParseConfiguration apply = null;
            for (Iterator<Map.Entry<String, Object>> iterator = construct.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                Object object = entry.getValue();
                if (!key.startsWith(PROPERTY_KEY)) {
                    continue;
                }

                String name = key.substring(1);
                MapParseConfigurationFunction function = parseMap.get(name);
                if (function == null) {
                    continue;
                }

                Map<String, Object> map;
                if (!(object instanceof Map)) {
                    map = new HashMap<>();
                    map.put("value", object);
                } else {
                    map = (Map<String, Object>) object;
                }

                apply = function.apply(node, map);
                iterator.remove();
                break;
            }

            if (apply != null) {
                construct.put(apply.getPath(), apply);
            }
            return construct;
        }
    }
}
