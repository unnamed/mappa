package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.region.Region;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;
import team.unnamed.mappa.util.Texts;

import java.util.function.Function;

public class RegionPlayerPart implements CommandPart {
    private final String name;
    private final RegionRegistry registry;
    private final Function<ArgumentStack, Region<?>> defaultRegion;
    private final Class<?> type;

    public RegionPlayerPart(String name,
                            RegionRegistry registry,
                            Function<ArgumentStack, Region<?>> defaultRegion,
                            Class<?> type) {
        this.name = name;
        this.registry = registry;
        this.defaultRegion = defaultRegion;
        this.type = type;
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
            Region<?> region = defaultRegion.apply(stack);
            context.setValue(this, region);
            return;
        }

        RegionSelection<?> selection = registry.getSelection(
            sender.getUniqueId().toString(),
            type);
        if (selection == null) {
            if (stack.hasNext()) {
                Region<?> region = defaultRegion.apply(stack);
                context.setValue(this, region);
                return;
            }

            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SELECTION
                    .withFormal("{type}", Texts.getTypeName(type)));
        }
        Object firstPoint = selection.getFirstPoint();
        if (firstPoint == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_FIRST_SELECTION
                    .withFormal("{type}", Texts.getTypeName(type)));
        }
        Object secondPoint = selection.getSecondPoint();
        if (secondPoint == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SECOND_SELECTION
                    .withFormal("{type}", Texts.getTypeName(type)));
        }
        context.setValue(this, registry.newRegion(selection));
    }
}
