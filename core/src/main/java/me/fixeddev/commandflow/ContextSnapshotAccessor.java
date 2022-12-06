package me.fixeddev.commandflow;

import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.part.CommandPart;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContextSnapshotAccessor {
    private final ContextSnapshot snapshot;

    public static Namespace getNamespaceOf(CommandContext context) {
        return new ContextSnapshotAccessor(context).namespace();
    }

    public ContextSnapshotAccessor(CommandContext context) {
        this.snapshot = context.getSnapshot();
    }

    public Namespace namespace() {
        return snapshot.namespace;
    }

    public Command executedCommand() {
        return snapshot.executedCommand;
    }

    public List<Command> commandExecutionPath() {
        return snapshot.commandExecutionPath;
    }

    public List<String> rawArguments() {
        return snapshot.rawArguments;
    }

    public List<String> labels() {
        return snapshot.labels;
    }

    public Set<CommandPart> allParts() {
        return snapshot.allParts;
    }

    public Map<String, List<CommandPart>> allPartsByName() {
        return snapshot.allPartsByName;
    }

    public Map<CommandPart, List<String>> rawBindings() {
        return snapshot.rawBindings;
    }

    public Map<CommandPart, List<Object>> valueBindings() {
        return snapshot.valueBindings;
    }
}
