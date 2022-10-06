package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.throwable.FindException;

import java.util.*;

@SuppressWarnings("unchecked")
public class MapPropertyPathPart implements ArgumentPart {
    public static final String PROPERTIES = "properties";

    private final String name;
    private final MappaBootstrap bootstrap;
    private final boolean findAll;
    private final boolean asProperty;

    public MapPropertyPathPart(String name, MappaBootstrap bootstrap, boolean findAll, boolean asProperty) {
        this.name = name;
        this.bootstrap = bootstrap;
        this.findAll = findAll;
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
        String path = stack.next();

        CommandSender sender = context.getObject(CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);
        if (!(sender instanceof Player)) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SESSION_SELECTED
                    .formalText());
        }
        Player player = (Player) sender;
        MapSession session = bootstrap.getSessionByEntity(player.getUniqueId());
        if (session == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SESSION_SELECTED
                    .formalText());
        } else if (!(session instanceof MapEditSession)) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SESSION_IS_SERIALIZED
                    .formalText(),
                session
            );
        }
        if (findAll) {
            try {
                MapEditSession editSession = (MapEditSession) session;
                MapPropertyTree tree = editSession.getProperties();

                int lastDot = path.lastIndexOf(".");
                String previousPath = lastDot == -1 ? "" : path.substring(0, lastDot);
                Map<String, Object> all = tree.findAll(previousPath);
                String node = path.substring(lastDot + 1);
                Object object = all.get(node);
                if (object == null) {
                    throw new ArgumentTextParseException(
                        TranslationNode.INVALID_PROPERTY
                            .withFormal("{property}", path));
                }

                if (asProperty) {
                    Map<String, MapProperty> properties = new HashMap<>();
                    Map<String, Object> map = object instanceof Map
                        ? (Map<String, Object>) object
                        : all;
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        Object value = entry.getValue();
                        if (!(value instanceof MapProperty)) {
                            continue;
                        }

                        properties.put(path + "." + entry.getKey(), (MapProperty) value);
                    }
                    context.setObject(Map.class, PROPERTIES, properties);
                }

                return Collections.singletonList(path);
            } catch (FindException e) {
                throw new ArgumentTextParseException(
                    TranslationNode.INVALID_PROPERTY
                        .withFormal("{property}", path));
            }
        } else {
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

                return Collections.singletonList(path);
            } catch (FindException e) {
                throw new ArgumentTextParseException(
                    TranslationNode.INVALID_PROPERTY
                        .withFormal("{property}", path));
            }
        }
    }

    @Override
    public @Nullable List<String> getSuggestions(CommandContext context,
                                                 ArgumentStack stack) {
        CommandSender sender = context.getObject(CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        MapSession session = bootstrap.getSessionByEntity(player.getUniqueId());
        if (!(session instanceof MapEditSession)) {
            return Collections.emptyList();
        }
        MapEditSession editSession = (MapEditSession) session;

        String arg = stack.hasNext() ? stack.next() : stack.current();
        List<String> suggestions = new ArrayList<>();
        int lastDot = arg.lastIndexOf(".");
        String path = lastDot == -1 ? "" : arg.substring(0, lastDot);

        Map<String, Object> all;
        try {
            MapPropertyTree tree = editSession.getProperties();
            all = new HashMap<>(tree.findAll(path));
            if (!findAll) {
                all.values().removeIf(o -> !(o instanceof MapProperty));
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
