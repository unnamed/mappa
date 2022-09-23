package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.part.Key;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.exception.CommandException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.Parts;
import me.fixeddev.commandflow.part.defaults.SubCommandPart;
import team.unnamed.mappa.internal.command.parts.OptionalDependentPart;
import team.unnamed.mappa.internal.event.MappaPropertySetEvent;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.*;
import team.unnamed.mappa.throwable.ParseException;

import java.lang.reflect.Type;
import java.util.*;

import static team.unnamed.mappa.util.Texts.toPrettifyString;

public class CommandSchemeNodeBuilderImpl implements CommandSchemeNodeBuilder {
    protected final Key sessionKey;
    protected final PartInjector injector;
    protected final MappaTextHandler textHandler;
    protected final EventBus eventBus;

    public CommandSchemeNodeBuilderImpl(Key sessionKey,
                                        PartInjector injector,
                                        MappaTextHandler textHandler,
                                        EventBus eventBus) {
        this.sessionKey = sessionKey;
        this.injector = injector;
        this.textHandler = textHandler;
        this.eventBus = eventBus;
    }

    @Override
    public PartInjector getInjector() {
        return injector;
    }

    @Override
    public Command fromScheme(MapScheme scheme) throws ParseException {
        Map<String, Command> nodeCommands = new HashMap<>();
        MapPropertyTree properties = scheme.getTreeProperties();
        for (String propertyPath : scheme.getObject(MapScheme.PLAIN_KEYS)) {
            MapProperty property = properties.find(propertyPath);

            Command command = fromProperty(propertyPath, property);

            // Resolving command parent
            int nodeIndex = propertyPath.lastIndexOf(".");
            // If property doesn't separate by dots
            // it would be an argument for the map scheme command.

            // If not, we need to resolve all the path as commands
            // and put them into the last argument command.
            String commandPath = nodeIndex == -1
                ? ""
                : propertyPath.substring(0, nodeIndex);
            Command nodeCommand = nodeCommands.get(commandPath);
            SubCommandPart subCommandPart;
            // Path command doesn't exists? lets create them
            if (nodeCommand == null) {
                Command pathCommand;
                subCommandPart = new SubCommandPart("subcommands", Collections.emptyList());
                if (commandPath.isEmpty()) {
                    nodeCommands.put("", newParentCommand(scheme, subCommandPart));
                } else {
                    // Get path previous last dot
                    int commandIndex = commandPath.lastIndexOf(".");
                    String name = commandIndex == -1
                        ? commandPath
                        : commandPath.substring(commandIndex + 1);
                    pathCommand = Command.builder(name)
                        .addPart(subCommandPart)
                        .permission(commandPath)
                        .build();
                    nodeCommands.put(commandPath, pathCommand);
                    // Map absolute path of command
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

    // idk how i made this
    // literally 4:00 a.m programming everything

    // refactor this if it is necessary
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
        if (schemeProperty.isImmutable()) {
            return fromImmutableProperty(path, schemeProperty);
        }

        CommandPart sessionPart = Commands.ofPart(injector, sessionKey);
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
            .action(new PropertyWriteAction(textHandler,
                eventBus,
                parts,
                sessionPart,
                delegate,
                viewFlag,
                clearFlag,
                path))
            .build();
    }

    private Command fromImmutableProperty(String path, MapProperty schemeProperty) {
        CommandPart sessionPart = Commands.ofPart(injector, sessionKey);
        List<CommandPart> parts = new ArrayList<>();
        parts.add(sessionPart);
        return Command.builder(schemeProperty.getName())
            .addParts(parts.toArray(new CommandPart[0]))
            .permission(path)
            .action(new PropertyReadAction(textHandler,
                sessionPart,
                path))
            .build();
    }

    public static class PropertyReadAction implements me.fixeddev.commandflow.command.Action {
        protected final MappaTextHandler textHandler;

        protected final CommandPart sessionPart;
        protected final String path;

        protected PropertyReadAction(MappaTextHandler textHandler,
                                     CommandPart sessionPart,
                                     String path) {
            this.textHandler = textHandler;
            this.sessionPart = sessionPart;
            this.path = path;
        }

        @Override
        public boolean execute(CommandContext context) throws CommandException {
            MapEditSession session = context
                .<MapEditSession>getValue(sessionPart)
                .orElseThrow(NullPointerException::new);
            MapProperty property = session.getProperty(path);
            Object sender = textHandler.getEntityFrom(context);
            viewProperty(sender, path, property);
            return true;
        }

        @SuppressWarnings("unchecked")
        protected void viewProperty(Object sender, String path, MapProperty property) {
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

        protected String getTypeName(Type type) {
            return type instanceof Class
                ? ((Class<?>) type).getSimpleName()
                : type.getTypeName();
        }

        protected String getPropertyTypeName(MapProperty property) {
            if (!(property instanceof MapCollectionProperty)) {
                return null;
            }

            MapCollectionProperty list = (MapCollectionProperty) property;
            return getTypeName(list.getCollectionType());
        }
    }

    public static class PropertyWriteAction extends PropertyReadAction {
        private final EventBus eventBus;
        private final List<CommandPart> parts;
        private final CommandPart delegate;
        private final CommandPart clearFlag;
        private final CommandPart viewFlag;

        public PropertyWriteAction(MappaTextHandler textHandler, EventBus eventBus) {
            this(textHandler,
                eventBus,
                null,
                null,
                null,
                null,
                null,
                null);
        }

        public PropertyWriteAction(MappaTextHandler textHandler,
                                   EventBus eventBus,
                                   List<CommandPart> parts,
                                   CommandPart sessionPart,
                                   CommandPart delegate,
                                   CommandPart viewFlag,
                                   CommandPart clearFlag,
                                   String path) {
            super(textHandler, sessionPart, path);
            this.eventBus = eventBus;
            this.parts = parts;
            this.delegate = delegate;
            this.viewFlag = viewFlag;
            this.clearFlag = clearFlag;
        }

        @Override
        public boolean execute(CommandContext context) throws CommandException {
            MapEditSession session = context
                .<MapEditSession>getValue(sessionPart)
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
                List<Text> translations;
                if (property instanceof MapCollectionProperty) {
                    Boolean remove = context.<Boolean>getValue(parts.get(0))
                        .orElse(null);
                    translations = Collections.singletonList(
                        actionCollection(path, session, newValue, remove)
                    );
                } else {
                    translations = actionSingle(path, session, newValue);
                }
                eventBus.callEvent(new MappaPropertySetEvent(sender, session, path, translations, property, false));
            } catch (ParseException e) {
                throw new CommandException(e);
            }
            return true;
        }

        public List<Text> actionSingle(String path,
                                       MapEditSession session,
                                       Object newValue) throws ParseException {
            Text node;
            List<Text> texts;
            session.property(path, newValue);
            if (newValue instanceof DeserializableList) {
                texts = new ArrayList<>();
                texts.add(TranslationNode
                    .PROPERTY_CHANGE_TO
                    .withFormal("{name}", path,
                        "{value}", ""));
                DeserializableList list = (DeserializableList) newValue;
                for (String value : list.deserialize()) {
                    texts.add(TranslationNode
                        .PROPERTY_LIST_ADDED_ENTRY
                        .with("{value}", value));
                }
            } else {
                String valueString = toPrettifyString(newValue);
                node = TranslationNode
                    .PROPERTY_CHANGE_TO
                    .withFormal("{name}", path,
                        "{value}", valueString);
                texts = Collections.singletonList(node);
            }
            return texts;
        }

        public Text actionCollection(String path,
                                     MapEditSession session,
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
            return node;
        }
    }
}
