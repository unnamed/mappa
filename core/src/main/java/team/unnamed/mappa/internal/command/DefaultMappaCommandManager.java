package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.*;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.command.modifiers.FallbackCommandModifiers;
import me.fixeddev.commandflow.exception.CommandException;
import me.fixeddev.commandflow.executor.Executor;
import me.fixeddev.commandflow.input.InputTokenizer;
import me.fixeddev.commandflow.translator.Translator;
import me.fixeddev.commandflow.usage.UsageBuilder;
import team.unnamed.mappa.internal.player.PlayerRegistry;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

import java.util.*;

public class DefaultMappaCommandManager implements MappaCommandManager {
    private final Map<String, Command> schemeCommands = new HashMap<>();
    private final CommandManager commandManager;
    private final CommandSchemeNodeBuilder nodeBuilder;

    public DefaultMappaCommandManager(CommandManager commandManager,
                                      CommandSchemeNodeBuilder nodeBuilder,
                                      PlayerRegistry<?> registry) {
        this.commandManager = commandManager;
        this.nodeBuilder = nodeBuilder;

        this.setAuthorizer(new MappaAuthorizer(getAuthorizer(), registry));
    }

    @Override
    public Command registerMapScheme(MapScheme scheme) throws ParseException {
        Command rootCommand = nodeBuilder.fromScheme(scheme);
        commandManager.registerCommand(rootCommand);
        schemeCommands.put(scheme.getName(), rootCommand);
        return rootCommand;
    }

    @Override
    public void unregisterMapScheme(MapScheme scheme) {
        Command command = schemeCommands.remove(scheme.getName());
        if (command == null) {
            return;
        }

        commandManager.unregisterCommand(command);
    }

    @Override
    public Command getRootCommand(MapScheme scheme) {
        return schemeCommands.get(scheme.getName());
    }

    @Override
    public Map<String, Command> getMapSchemeCommands() {
        return schemeCommands;
    }

    @Override
    public CommandManager getInternalCommandManager() {
        return commandManager;
    }

    @Override
    public CommandSchemeNodeBuilder getNodeBuilder() {
        return nodeBuilder;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return commandManager.getErrorHandler();
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        commandManager.setErrorHandler(errorHandler);
    }

    @Override
    public FallbackCommandModifiers getCommandModifiers() {
        return commandManager.getCommandModifiers();
    }

    @Override
    public Optional<Command> getCommand(String name) {
        return commandManager.getCommand(name);
    }

    @Override
    public boolean execute(CommandContext commandContext) throws CommandException {
        return commandManager.execute(commandContext);
    }

    @Override
    public boolean execute(Namespace namespace, List<String> list) throws CommandException {
        return commandManager.execute(namespace, list);
    }

    @Override
    public ParseResult parse(Namespace namespace, List<String> list) throws CommandException {
        return commandManager.parse(namespace, list);
    }

    @Override
    public List<String> getSuggestions(Namespace namespace, List<String> list) {
        return commandManager.getSuggestions(namespace, list);
    }

    @Override
    public boolean execute(Namespace namespace, String name) throws CommandException {
        return commandManager.execute(namespace, name);
    }

    @Override
    public List<String> getSuggestions(Namespace namespace, String name) {
        return commandManager.getSuggestions(namespace, name);
    }

    @Override
    public ParseResult parse(Namespace namespace, String name) throws CommandException {
        return commandManager.parse(namespace, name);
    }

    @Override
    public void registerCommand(Command command) {
        commandManager.registerCommand(command);
    }

    @Override
    public void registerCommand(String name, Command command) {
        commandManager.registerCommand(name, command);
    }

    @Override
    public void registerCommands(List<Command> list) {
        commandManager.registerCommands(list);
    }

    @Override
    public void unregisterCommand(Command command) {
        commandManager.unregisterCommand(command);
    }

    @Override
    public void unregisterCommands(List<Command> list) {
        commandManager.unregisterCommands(list);
    }

    @Override
    public void unregisterAll() {
        commandManager.unregisterAll();
        schemeCommands.clear();
    }

    @Override
    public Set<Command> getCommands() {
        return commandManager.getCommands();
    }

    @Override
    public boolean exists(String name) {
        return commandManager.exists(name);
    }

    @Override
    public Authorizer getAuthorizer() {
        return commandManager.getAuthorizer();
    }

    @Override
    public void setAuthorizer(Authorizer authorizer) {
        commandManager.setAuthorizer(authorizer);
    }

    @Override
    public InputTokenizer getInputTokenizer() {
        return commandManager.getInputTokenizer();
    }

    @Override
    public void setInputTokenizer(InputTokenizer inputTokenizer) {
        commandManager.setInputTokenizer(inputTokenizer);
    }

    @Override
    public Executor getExecutor() {
        return commandManager.getExecutor();
    }

    @Override
    public void setExecutor(Executor executor) {
        commandManager.setExecutor(executor);
    }

    @Override
    public Translator getTranslator() {
        return commandManager.getTranslator();
    }

    @Override
    public void setTranslator(Translator translator) {
        commandManager.setTranslator(translator);
    }

    @Override
    public UsageBuilder getUsageBuilder() {
        return commandManager.getUsageBuilder();
    }

    @Override
    public void setUsageBuilder(UsageBuilder usageBuilder) {
        commandManager.setUsageBuilder(usageBuilder);
    }
}
