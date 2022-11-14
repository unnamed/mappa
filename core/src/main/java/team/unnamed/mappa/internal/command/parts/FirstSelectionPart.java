package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.util.Texts;

import java.util.function.Function;

public class FirstSelectionPart<T> implements CommandPart {
    private final String name;
    private final RegionRegistry registry;
    private final Function<ArgumentStack, T> defaultSelection;
    private final Class<T> type;

    public FirstSelectionPart(String name,
                              RegionRegistry registry,
                              Function<ArgumentStack, T> defaultSelection,
                              Class<T> type) {
        this.name = name;
        this.type = type;
        this.registry = registry;
        this.defaultSelection = defaultSelection;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart part
    ) throws ArgumentParseException {
        MappaPlayer sender = context.getObject(MappaPlayer.class,
            MappaCommandManager.MAPPA_PLAYER);
        if (sender.isConsole()) {
            Object o = defaultSelection.apply(stack);
            context.setValue(this, o);
            return;
        }

        String id = sender.getUniqueId().toString();
        RegionSelection<T> selection = registry.getSelection(id, type);
        if (selection == null) {
            if (stack.hasNext()) {
                Object o = defaultSelection.apply(stack);
                context.setValue(this, o);
                return;
            }

            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SELECTION
                    .withFormal("{type}", Texts.getTypeName(type)));
        }
        T firstPoint = selection.getFirstPoint();
        if (firstPoint == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_FIRST_SELECTION
                    .withFormal("{type}", Texts.getTypeName(type)));
        }
        context.setValue(this, firstPoint);
    }
}
