package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.exception.NoMoreArgumentsException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.SinglePartWrapper;
import me.fixeddev.commandflow.stack.ArgumentStack;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class OptionalDependentPart implements CommandPart, SinglePartWrapper {
    private final CommandPart delegate;
    private final List<CommandPart> dependencies;

    public OptionalDependentPart(CommandPart delegate, CommandPart... dependencies) {
        this(delegate, Arrays.asList(dependencies));
    }

    public OptionalDependentPart(CommandPart delegate, List<CommandPart> dependencies) {
        this.delegate = delegate;
        this.dependencies = dependencies;
    }

    @Override
    public void parse(CommandContext context, ArgumentStack stack, CommandPart caller)
        throws ArgumentParseException {
        try {
            delegate.parse(context, stack, caller);
        } catch (ArgumentParseException | NoMoreArgumentsException e) {
            for (CommandPart dependency : dependencies) {
                Object value = context.getValue(dependency)
                    .orElse(null);
                if (value instanceof Boolean) {
                    boolean bool = (boolean) value;
                    if (bool) {
                        return;
                    }
                } else if (value != null) {
                    return;
                }
            }

            throw e;
        }
    }

    public List<CommandPart> getDependencies() {
        return dependencies;
    }

    @Override
    public @Nullable Component getLineRepresentation() {
        Component component = delegate.getLineRepresentation();
        return component == null
            ? null
            : TextComponent.builder("[")
            .append(component)
            .append("]")
            .build();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public CommandPart getPart() {
        return delegate;
    }
}
