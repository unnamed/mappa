package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.command.parts.ChunkCuboidPart;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;

public class ChunkCuboidPlayerPart extends ChunkCuboidPart {
    private final RegionRegistry registry;

    public ChunkCuboidPlayerPart(String name, RegionRegistry registry) {
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
        RegionSelection<Chunk> selection = registry.getChunkSelection(id);
        context.setValue(this, registry.newRegion(selection));
    }
}