package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.CommandContext;
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
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.visualizer.Visualizer;
import team.unnamed.mappa.throwable.ParseException;

import java.util.*;

import static team.unnamed.mappa.internal.command.MappaCommandManager.SESSION_KEY;

public class CommandSchemeNodeBuilderImpl implements CommandSchemeNodeBuilder {

    protected final PartInjector injector;
    protected final MappaTextHandler textHandler;
    protected final EventBus eventBus;
    protected final Visualizer visualizer;

    public CommandSchemeNodeBuilderImpl(PartInjector injector,
                                        MappaTextHandler textHandler,
                                        EventBus eventBus,
                                        Visualizer visualizer) {
        this.injector = injector;
        this.textHandler = textHandler;
        this.eventBus = eventBus;
        this.visualizer = visualizer;
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
            // If property doesn't have dots
            // it would be an argument for the map scheme command.

            // If not, we need to resolve all the path as commands
            // and put them into the last argument command.
            String commandPath = nodeIndex == -1
                ? ""
                : propertyPath.substring(0, nodeIndex);
            Command nodeCommand = nodeCommands.get(commandPath);
            SubCommandPart subCommandPart;
            // Path command not exists? lets create them
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
                "subcommands", // default subcommand part name
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

        CommandPart sessionPart = Commands.ofPart(injector, SESSION_KEY);
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
        CommandPart infoFlag = Parts.switchPart("info", "i", true);
        Collections.addAll(flags,
            clearFlag,
            infoFlag);
        if (visualizer != null) {
            CommandPart showFlag = Parts.switchPart("show", "s", true);
            flags.add(showFlag);
        }
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
                infoFlag,
                clearFlag,
                path))
            .build();
    }

    private Command fromImmutableProperty(String path, MapProperty schemeProperty) {
        CommandPart sessionPart = Commands.ofPart(injector, SESSION_KEY);
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
            MappaPlayer sender = context.getObject(MappaPlayer.class, MappaCommandManager.MAPPA_PLAYER);
            viewProperty(sender, path, property);
            return true;
        }

        protected void viewProperty(MappaPlayer sender, String path, MapProperty property) {
            sender.showPropertyInfo(path, property);
        }
    }

    public static class PropertyWriteAction extends PropertyReadAction {
        private final EventBus eventBus;
        private final List<CommandPart> parts;
        private final CommandPart delegate;
        private final CommandPart clearFlag;
        private final CommandPart viewFlag;

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
            MappaPlayer sender = context.getObject(MappaPlayer.class, MappaCommandManager.MAPPA_PLAYER);
            boolean view = context
                .<Boolean>getValue(viewFlag)
                .orElse(false);
            if (view) {
                sender.showPropertyInfo(path);
                return true;
            }

            boolean clear = context
                .<Boolean>getValue(clearFlag)
                .orElse(false);
            if (clear) {
                sender.clearProperty(path);
                return true;
            }

            Object newValue = context
                .getValue(delegate)
                .orElseThrow(NullPointerException::new);
            try {
                Boolean remove = context.<Boolean>getValue(parts.get(0))
                    .orElse(null);
                if (Boolean.TRUE.equals(remove)) {
                    sender.removePropertyValue(path, newValue);
                } else {
                    sender.setProperty(path, newValue);
                }
                eventBus.callEvent(
                    new MappaPropertySetEvent(sender,
                        session,
                        path,
                        session.getProperty(path),
                        false));
            } catch (ParseException e) {
                throw new CommandException(e);
            }
            return true;
        }
    }
}
