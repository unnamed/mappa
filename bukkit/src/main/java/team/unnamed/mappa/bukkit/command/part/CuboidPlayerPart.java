package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.util.Texts;
import team.unnamed.mappa.internal.command.parts.CuboidPart;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

public class CuboidPlayerPart extends CuboidPart {
    private final RegionRegistry registry;

    public CuboidPlayerPart(String name, RegionRegistry registry) {
        super(name);
        this.registry = registry;
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart part
    ) throws ArgumentParseException {
        CommandSender sender = context.getObject(CommandSender.class,
            BukkitCommandManager.SENDER_NAMESPACE);
        if (!(sender instanceof Player)) {
            super.parse(context, stack, part);
            return;
        }

        Player player = (Player) sender;
        String id = player.getUniqueId().toString();
        RegionSelection<Vector> selection = registry.getVectorSelection(id);
        if (selection == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SELECTION
                    .withFormal("{type}", Texts.getTypeName(Vector.class)));
        }
        Vector firstPoint = selection.getFirstPoint();
        if (firstPoint == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_FIRST_SELECTION
                    .withFormal("{type}", Texts.getTypeName(Vector.class)));
        }
        Vector secondPoint = selection.getSecondPoint();
        if (secondPoint == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .NO_SECOND_SELECTION
                    .withFormal("{type}", Texts.getTypeName(Vector.class)));
        }
        context.setValue(this, registry.newRegion(selection));
    }
}
