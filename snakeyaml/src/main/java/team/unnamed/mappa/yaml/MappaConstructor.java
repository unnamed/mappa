package team.unnamed.mappa.yaml;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import team.unnamed.mappa.model.map.configuration.InterpretMode;
import team.unnamed.mappa.model.map.configuration.MultiNodeParseConfiguration;
import team.unnamed.mappa.model.map.configuration.NodeParentParseConfiguration;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;
import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.ChunkCuboid;
import team.unnamed.mappa.object.Cuboid;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.yaml.function.MapParseConfigurationFunction;
import team.unnamed.mappa.yaml.function.TagFunction;

import java.util.*;

public class MappaConstructor extends SafeConstructor {
    public static final String PROPERTY_KEY = "$";
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

    public boolean isOptional(Node node) {
        Mark startMark = node.getStartMark();
        // Pointer starts in the first character of value index:
        //          ▼ First value char
        //   node?: value
        //       ▲ Last node char
        // To get the last character of the name node we subtract 3 from the pointer.
        int lastCharacter = buffer.charAt(startMark.getIndex() - 3);
        return new StringBuilder()
            .appendCodePoint(lastCharacter)
            .toString()
            .equals("?");
    }

    public MappaConstructor() {
        registerTag("property", (node, args) -> SchemeNode.newNode(String.class, false, args));
        registerTag("boolean", (node, args) -> SchemeNode.newNode(boolean.class, isOptional(node), args.split(" ")));
        registerTag("int", (node, args) -> SchemeNode.newNode(int.class, isOptional(node), args.split(" ")));
        registerTag("long", (node, args) -> SchemeNode.newNode(long.class, isOptional(node), args.split(" ")));
        registerTag("double", (node, args) -> SchemeNode.newNode(double.class, isOptional(node), args.split(" ")));
        registerTag("float", (node, args) -> SchemeNode.newNode(float.class, isOptional(node), args.split(" ")));
        registerTag("string", (node, args) -> SchemeNode.newNode(String.class, isOptional(node)));
        registerTag("char", (node, args) -> SchemeNode.newNode(char.class, isOptional(node)));
        registerTag("list", (node, args) -> {
            SchemeNode typeNode;
            if (args == null || args.isEmpty()) {
                typeNode = SchemeNode.newNode(Object.class, false);
            } else {
                String[] arrayArgs = args.split(" ");
                if (arrayArgs.length < 2) {
                    throw new IllegalArgumentException("Incomplete sentence for list type: " + args);
                }
                String tagName = arrayArgs[1];
                TagFunction function = tags.get(tagName);
                String[] subArgs = arrayArgs.length > 2
                    ? newSubArray(arrayArgs, 2)
                    : new String[0];
                Object result = function.apply(node, String.join(" ", subArgs));
                typeNode = result instanceof SchemeNode
                    ? (SchemeNode) result
                    : SchemeNode.newNode(Object.class, false);
            }

            return SchemeNode.newCollection(List.class, typeNode);
        });
        registerTag("vector", (node, args) -> SchemeNode.newNode(Vector.class, isOptional(node), args.split(" ")));
        registerTag("cuboid", (node, args) -> SchemeNode.newNode(Cuboid.class, isOptional(node), args.split(" ")));
        registerTag("chunk", (node, args) -> SchemeNode.newNode(Chunk.class, isOptional(node), args.split(" ")));
        registerTag("chunk-cuboid", (node, args) -> SchemeNode.newNode(ChunkCuboid.class, isOptional(node), args.split(" ")));

        registerProperty("parent", (node, map) -> {
            String interpretString = (String) map.get("interpret");
            InterpretMode mode = InterpretMode.valueOf(interpretString.toUpperCase());
            String formatName = (String) map.get("format-parent-name");
            Object objectAlias = map.get("aliases");
            String[] aliases = null;
            if (objectAlias instanceof String) {
                aliases = new String[]{(String) objectAlias};
            } else if (objectAlias instanceof String[]) {
                aliases = (String[]) objectAlias;
            }

            return new NodeParentParseConfiguration(mode, formatName, aliases);
        });
        registerProperty("multi-node", (node, map) ->
            new MultiNodeParseConfiguration((List<String>) map.get("value")));

        this.yamlConstructors.put(Tag.MAP, new ConstructMappaProperty(parseConfigurationMap));
    }

    private String[] newSubArray(String[] array, int start) {
        String[] subArray = new String[array.length - start];
        int slot = 0;
        for (int i = start; i < array.length; i++) {
            subArray[slot++] = array[start];
        }
        return subArray;
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

    public class ConstructStringTag extends SafeConstructor.ConstructYamlStr {
        private final TagFunction toEntity;

        public ConstructStringTag(TagFunction toEntity) {
            this.toEntity = toEntity;
        }

        @Override
        public Object construct(Node node) {
            String construct = (String) super.construct(node);
            return toEntity.apply(node, construct);
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
                construct.put(apply.getNode(), apply);
            }
            return construct;
        }
    }
}
