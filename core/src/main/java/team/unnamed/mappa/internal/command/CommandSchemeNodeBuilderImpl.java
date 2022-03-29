package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.defaults.SubCommandPart;
import team.unnamed.mappa.function.EntityProvider;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommandSchemeNodeBuilderImpl implements CommandSchemeNodeBuilder {
    protected final PartInjector injector;
    protected final MappaTextHandler textHandler;
    protected final EntityProvider provider;

    public CommandSchemeNodeBuilderImpl(PartInjector injector,
                                        MappaTextHandler textHandler,
                                        EntityProvider provider) {
        this.injector = injector;
        this.textHandler = textHandler;
        this.provider = provider;
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

    protected Command fromPath(String absolutePath, String path, CommandPart part) {
        return Command.builder(path)
            .addPart(part)
            .permission(absolutePath)
            .build();
    }

    @Override
    public Command fromProperty(String path, MapProperty property) {
        CommandPart part = Commands.ofPart(injector, property.getType());
        return Command.builder(property.getName())
            .addPart(part)
            .permission(path)
            .action(context -> {
                Object newValue = context.getValue(part)
                    .orElseThrow(NullPointerException::new);
                property.parseValue(newValue);
                Object sender = provider.fromContext(context);
                TextNode node = TranslationNode.PROPERTY_CHANGE_TO.withFormal(
                    "{name}", property.getName(),
                    "{value}", newValue
                );
                textHandler.send(sender, node);
                return true;
            })
            .build();
    }
}
