package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.exception.CommandException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.Parts;
import me.fixeddev.commandflow.part.defaults.SubCommandPart;
import team.unnamed.mappa.internal.command.parts.OptionalDependentPart;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.ParseException;

import java.lang.reflect.Type;
import java.util.*;

public class CommandSchemeNodeBuilderImpl implements CommandSchemeNodeBuilder {
    protected final PartInjector injector;
    protected final MappaTextHandler textHandler;

    public CommandSchemeNodeBuilderImpl(PartInjector injector,
                                        MappaTextHandler textHandler) {
        this.injector = injector;
        this.textHandler = textHandler;
    }

    @Override
    public Command fromScheme(MapScheme scheme) {
        Map<String, MapProperty> properties = scheme.getProperties();
        Map<String, Command> nodeCommands = new HashMap<>();
        for (Map.Entry<String, MapProperty> entry : properties.entrySet()) {
            MapProperty property = entry.getValue();

            String propertyPath = entry.getKey();
            Command command = fromProperty(propertyPath, property);

            int nodeIndex = propertyPath.lastIndexOf(".");
            String commandPath = nodeIndex == -1
                ? ""
                : propertyPath.substring(0, nodeIndex);
            Command nodeCommand = nodeCommands.get(commandPath);
            SubCommandPart subCommandPart;
            if (nodeCommand == null) {
                Command pathCommand;
                subCommandPart = new SubCommandPart("subcommands", Collections.emptyList());
                if (commandPath.isEmpty()) {
                    nodeCommands.put("", newParentCommand(scheme, subCommandPart));
                } else {
                    int commandIndex = commandPath.lastIndexOf(".");
                    String name = commandIndex == -1
                        ? commandPath
                        : commandPath.substring(commandIndex + 1);
                    pathCommand = Command.builder(name)
                        .addPart(subCommandPart)
                        .permission(commandPath)
                        .build();
                    nodeCommands.put(commandPath, pathCommand);
                    mapAllPath(scheme,
                        nodeCommands,
                        commandPath,
                        pathCommand);
                }
            } else {
                subCommandPart = (SubCommandPart) nodeCommand.getPart();
            }

            Map<String, Command> subCommands = subCommandPart.getSubCommandMap();
            subCommands.put(command.getName(), command);
        }

        // Empty string to represent the top of the scheme
        return nodeCommands.get("");
    }

    protected void mapAllPath(MapScheme scheme,
                              Map<String, Command> map,
                              String path,
                              Command previousCommand) {
        int lastDot = path.lastIndexOf(".");
        String name;
        String parentPath;
        if (lastDot == -1) {
            name = path;
            parentPath = "";
        } else {
            parentPath = path.substring(0, lastDot);
            int parentLastDot = parentPath.lastIndexOf(".");
            if (parentLastDot == -1) {
                name = parentPath;
            } else {
                name = parentPath.substring(0, parentLastDot);
            }
        }

        Command parent = map.get(parentPath);
        Map<String, Command> subCommandMap;
        SubCommandPart part;
        if (parent != null) {
            part = (SubCommandPart) parent.getPart();
        } else {
            part = new SubCommandPart(
                "subcommands",
                Collections.singletonList(previousCommand));
            if (parentPath.isEmpty()) {
                parent = newParentCommand(scheme, part);
                map.put(parentPath, parent);
            } else {
                parent = Command.builder(name)
                    .addPart(part)
                    .permission(parentPath)
                    .build();
                map.put(parentPath, parent);
            }
        }
        subCommandMap = part.getSubCommandMap();

        subCommandMap.put(previousCommand.getName(), previousCommand);
        if (!parentPath.isEmpty()) {
            mapAllPath(scheme, map, parentPath, parent);
        }
    }

    protected Command newParentCommand(MapScheme scheme, SubCommandPart part) {
        String[] aliases = scheme.getAliases();
        return Command.builder(scheme.getName().toLowerCase())
            .addPart(part)
            .aliases(aliases == null ? new String[0] : aliases)
            .build();
    }

    @Override
    public Command fromProperty(String path, MapProperty schemeProperty) {
        CommandPart sessionPart = Commands.ofPart(injector, MapSession.class);
        List<CommandPart> flags = new ArrayList<>();
        CommandPart delegate = Commands.ofPart(injector, schemeProperty.getType());
        CommandPart wrapperPart = new OptionalDependentPart(
            delegate,
            flags
        );

        List<CommandPart> parts = new ArrayList<>();
        if (schemeProperty instanceof MapCollectionProperty) {
            CommandPart removeFlag = Parts.switchPart("remove", "r", true);
            parts.add(removeFlag);
        }
        CommandPart clearFlag = Parts.switchPart("clear", "c", true);
        CommandPart viewFlag = Parts.switchPart("view", "v", true);
        Collections.addAll(flags,
            clearFlag,
            viewFlag);
        parts.addAll(flags);

        Collections.addAll(parts,
            sessionPart,
            wrapperPart);
        return Command.builder(schemeProperty.getName())
            .addParts(parts.toArray(new CommandPart[0]))
            .permission(path)
            .action(new PropertyAction(textHandler,
                parts,
                sessionPart,
                delegate,
                viewFlag,
                clearFlag,
                path))
            .build();
    }

    public static class PropertyAction implements me.fixeddev.commandflow.command.Action {
        private final MappaTextHandler textHandler;

        private final List<CommandPart> parts;
        private final CommandPart sessionPart;
        private final CommandPart delegate;
        private final CommandPart viewFlag;
        private final CommandPart clearFlag;
        private final String path;

        public PropertyAction(MappaTextHandler textHandler) {
            this(textHandler,
                null,
                null,
                null,
                null,
                null,
                null);
        }

        public PropertyAction(MappaTextHandler textHandler,
                              List<CommandPart> parts,
                              CommandPart sessionPart,
                              CommandPart delegate,
                              CommandPart viewFlag,
                              CommandPart clearFlag,
                              String path) {
            this.textHandler = textHandler;
            this.parts = parts;
            this.sessionPart = sessionPart;
            this.delegate = delegate;
            this.viewFlag = viewFlag;
            this.clearFlag = clearFlag;
            this.path = path;
        }

        @Override
        public boolean execute(CommandContext context) throws CommandException {
            MapSession session = context
                .<MapSession>getValue(sessionPart)
                .orElseThrow(NullPointerException::new);
            MapProperty property = session.getProperty(path);
            Object sender = textHandler.getEntityFrom(context);
            boolean view = context
                .<Boolean>getValue(viewFlag)
                .orElse(false);
            if (view) {
                viewProperty(sender, path, property);
                return true;
            }

            boolean clear = context
                .<Boolean>getValue(clearFlag)
                .orElse(false);
            if (clear) {
                property.clearValue();
                textHandler.send(sender,
                    TranslationNode
                        .PROPERTY_CLEAR
                        .withFormal("{name}", path));
                return true;
            }

            Object newValue = context
                .getValue(delegate)
                .orElseThrow(NullPointerException::new);
            try {
                if (property instanceof MapCollectionProperty) {
                    Boolean remove = context.<Boolean>getValue(parts.get(0))
                        .orElse(null);
                    actionList(sender, path, session, newValue, remove);
                } else {
                    actionSingle(sender, path, session, newValue);
                }
            } catch (ParseException e) {
                throw new CommandException(e);
            }
            return true;
        }

        public void viewProperty(Object sender, String path, MapProperty property) {
            TextNode header = TranslationNode
                .PROPERTY_INFO_HEADER
                .with("{name}", property.getName());
            textHandler.send(sender, header);
            textHandler.send(sender,
                TranslationNode
                    .PROPERTY_INFO_PATH
                    .with("{path}", path));
            String typeName = getTypeName(property.getType());
            String propertyType = getPropertyTypeName(property);
            if (propertyType != null) {
                typeName += " (" + propertyType + ")";
            }
            textHandler.send(sender,
                TranslationNode
                    .PROPERTY_INFO_TYPE
                    .with("{type}", typeName));
            Object value = property.getValue();
            if (property instanceof MapCollectionProperty) {
                Collection<Object> collection = Objects.requireNonNull((Collection<Object>) value);
                textHandler.send(sender,
                    TranslationNode
                        .PROPERTY_INFO_VALUE
                        .with("{value}", collection.isEmpty() ? "null" : ""));
                for (Object entry : collection) {
                    textHandler.send(sender,
                        TranslationNode
                            .PROPERTY_INFO_VALUE_LIST
                            .with("{value}", toPrettifyString(entry)));
                }
            } else {
                if (value == null) {
                    value = "null";
                } else {
                    value = toPrettifyString(value);
                }
                textHandler.send(sender,
                    TranslationNode
                        .PROPERTY_INFO_VALUE
                        .with("{value}", value));
            }
            textHandler.send(sender, header);
        }

        public void actionSingle(Object sender,
                                 String path,
                                 MapSession session,
                                 Object newValue) throws ParseException {
            session.property(path, newValue);
            if (newValue instanceof DeserializableList) {
                textHandler.send(sender, TranslationNode
                    .PROPERTY_CHANGE_TO
                    .withFormal("{name}", path,
                        "{value}", "")
                );
                DeserializableList list = (DeserializableList) newValue;
                for (String value : list.deserialize()) {
                    textHandler.send(sender,
                        TranslationNode
                            .PROPERTY_LIST_ADDED_ENTRY
                            .with("{value}", value));
                }
                return;
            }
            String valueString = toPrettifyString(newValue);
            TextNode node = TranslationNode
                .PROPERTY_CHANGE_TO
                .withFormal("{name}", path,
                    "{value}", valueString);
            textHandler.send(sender, node);
        }

        public void actionList(Object sender,
                               String path,
                               MapSession session,
                               Object newValue,
                               Boolean remove) throws ParseException {
            Text node;
            if (remove != null && remove) {
                boolean found = session.removePropertyValue(path, newValue);
                String valueString = toPrettifyString(newValue);
                TranslationNode translate;
                if (found) {
                    translate = TranslationNode.PROPERTY_LIST_REMOVED;
                    MapProperty property = session.getProperty(path);
                    node = translate
                        .withFormal("{type}", getTypeName(property.getType()),
                            "{name}", path,
                            "{value}", valueString);
                } else {
                    translate = TranslationNode.PROPERTY_LIST_VALUE_NOT_FOUND;
                    node = translate
                        .withFormal("{name}", path,
                            "{value}", valueString);
                }
            } else {
                session.property(path, newValue);
                String valueString = toPrettifyString(newValue);
                MapProperty property = session.getProperty(path);
                node = TranslationNode
                    .PROPERTY_LIST_ADDED
                    .withFormal("{type}", getTypeName(property.getType()),
                        "{name}", path,
                        "{value}", valueString
                    );
            }
            textHandler.send(sender, node);
        }

        public String toPrettifyString(Object o) {
            if (o instanceof Deserializable) {
                Deserializable deserializable = (Deserializable) o;
                return deserializable.deserialize();
            } else {
                return String.valueOf(o);
            }
        }

        public String getTypeName(Type type) {
            return type instanceof Class
                ? ((Class<?>) type).getSimpleName()
                : type.getTypeName();
        }

        public String getPropertyTypeName(MapProperty property) {
            if (!(property instanceof MapCollectionProperty)) {
                return null;
            }

            MapCollectionProperty list = (MapCollectionProperty) property;
            return getTypeName(list.getCollectionType());
        }
    }
}
