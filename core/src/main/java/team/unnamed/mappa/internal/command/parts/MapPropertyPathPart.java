package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.FindException;

import java.util.*;

@SuppressWarnings("unchecked")
public class MapPropertyPathPart implements ArgumentPart {

    public enum PropertyType {
        PROPERTY(MapProperty.class),
        COLLECTION(MapCollectionProperty.class),
        SECTION(Map.class),
        ALL(null);

        private final Class<?> clazz;


        PropertyType(Class<?> clazz) {
            this.clazz = clazz;
        }

        public boolean typeEquals(Object o) {
            return o != null && (clazz == null || clazz.isAssignableFrom(o.getClass()));
        }
    }

    public static final String PROPERTIES = "properties";
    public static final String MAPS = "maps";

    private final String name;
    private final PropertyType findType;
    private final boolean asProperty;

    public MapPropertyPathPart(String name, PropertyType findType, boolean asProperty) {
        this.name = name;
        this.findType = findType;
        this.asProperty = asProperty;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<?> parseValue(CommandContext context,
                              ArgumentStack stack,
                              CommandPart part)
        throws ArgumentParseException {
        MappaPlayer sender = context.getObject(MappaPlayer.class,
            MappaCommandManager.MAPPA_PLAYER);
        if (sender.isConsole()) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SESSION_SELECTED
                    .formalText());
        }
        MapSession session = sender.getMapSession();
        if (session == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SESSION_SELECTED
                    .formalText());
        }
        String path;
        if (findType == PropertyType.ALL) {
            path = stack.hasNext() ? stack.next() : "";
            try {
                MapEditSession editSession = (MapEditSession) session;
                MapPropertyTree tree = editSession.getProperties();

                int lastDot = path.lastIndexOf(".");
                String previousPath = lastDot == -1 ? "" : path.substring(0, lastDot);
                Map<String, Object> all = tree.findAll(previousPath);
                String node = path.substring(lastDot + 1);
                Object anyObject = all.get(node);
                if (anyObject == null) {
                    throw new ArgumentTextParseException(
                        TranslationNode.INVALID_PROPERTY
                            .withFormal("{property}", path));
                } else if (anyObject instanceof Map) {
                    all = (Map<String, Object>) anyObject;
                }

                if (asProperty) {
                    Map<String, MapProperty> properties = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> entry : all.entrySet()) {
                        Object value = entry.getValue();
                        if (!(value instanceof MapProperty)) {
                            continue;
                        }

                        String absolutePath = path + "." + entry.getKey();
                        properties.put(absolutePath, (MapProperty) value);
                    }
                    context.setObject(Map.class, PROPERTIES, properties);
                }
            } catch (FindException e) {
                throw new ArgumentTextParseException(
                    TranslationNode.INVALID_PROPERTY
                        .withFormal("{property}", path));
            }

        } else if (findType == PropertyType.SECTION) {
            path = stack.hasNext() ? stack.next() : "";
            try {
                MapEditSession editSession = (MapEditSession) session;
                MapPropertyTree tree = editSession.getProperties();

                Map<String, Object> all = tree.findAll(path);
                if (asProperty) {
                    Map<String, Object> properties = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> entry : all.entrySet()) {
                        Object value = entry.getValue();

                        String key = entry.getKey();
                        String absolutePath = path.isEmpty() ? key : path + "." + key;
                        properties.put(absolutePath, value);
                    }
                    context.setObject(Map.class, MAPS, properties);
                }
            } catch (FindException e) {
                throw new ArgumentTextParseException(
                    TranslationNode.INVALID_PROPERTY
                        .withFormal("{property}", path));
            }
        } else {
            path = stack.next();
            try {
                MapEditSession editSession = (MapEditSession) session;
                MapPropertyTree tree = editSession.getProperties();

                // Throws exception if property not found
                MapProperty property = tree.find(path);
                if (asProperty) {
                    Map<String, MapProperty> properties = new HashMap<>();
                    properties.put(path, property);
                    context.setObject(Map.class, PROPERTIES, properties);
                }
            } catch (FindException e) {
                throw new ArgumentTextParseException(
                    TranslationNode.INVALID_PROPERTY
                        .withFormal("{property}", path));
            }
        }

        return Collections.singletonList(path);
    }

    @Override
    public @Nullable List<String> getSuggestions(CommandContext context,
                                                 ArgumentStack stack) {
        MappaPlayer sender = context.getObject(MappaPlayer.class,
            MappaCommandManager.MAPPA_PLAYER);
        if (sender.isConsole()) {
            return Collections.emptyList();
        }

        MapSession session = sender.getMapSession();
        String arg = stack.hasNext() ? stack.next() : stack.current();
        List<String> suggestions = new ArrayList<>();
        int lastDot = arg.lastIndexOf(".");
        String path = lastDot == -1 ? "" : arg.substring(0, lastDot);

        Map<String, Object> all;
        try {
            MapPropertyTree tree = session.getProperties();
            all = new HashMap<>(tree.findAll(path));
            if (findType != PropertyType.ALL) {
                all.values().removeIf(o -> !findType.typeEquals(o));
            }
        } catch (FindException e) {
            return Collections.emptyList();
        }

        String node = lastDot == -1 ? arg : arg.substring(lastDot + 1);
        for (String key : all.keySet()) {
            if (!key.startsWith(node)) {
                continue;
            }

            if (!path.isEmpty()) {
                key = path + "." + key;
            }
            suggestions.add(key);
        }
        return suggestions;
    }
}
