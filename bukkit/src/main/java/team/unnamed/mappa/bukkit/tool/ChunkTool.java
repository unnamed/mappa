package team.unnamed.mappa.bukkit.tool;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.bukkit.util.Texts;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.internal.tool.Tool;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Chunk;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.Vector;

public class ChunkTool extends AbstractBukkitTool {

    public ChunkTool(String id,
                     boolean interactAir,
                     RegionRegistry regionRegistry,
                     MappaTextHandler textHandler) {
        super(id,
            interactAir,
            regionRegistry,
            textHandler);
    }

    public ChunkTool(RegionRegistry regionRegistry, MappaTextHandler textHandler) {
        super(ToolHandler.CHUNK_TOOL,
            false,
            regionRegistry,
            textHandler);
    }

    @Override
    public void interact(Player entity, Vector lookingAt, Button button, boolean shift) {
        String uniqueId = entity.getUniqueId().toString();
        RegionSelection<Chunk> chunkSelection =
            regionRegistry.getOrNewChunkSelection(uniqueId);

        World world = entity.getWorld();
        Location location = new Location(world, lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
        Chunk chunkMappa = MappaBukkit.toMappa(location.getChunk());
        BukkitTranslationNode text;
        float soundPitch;
        if (button == Tool.Button.RIGHT) {
            chunkSelection.setFirstPoint(chunkMappa);
            text = BukkitTranslationNode.FIRST_POINT_SELECTED;
            soundPitch = 0.5F;
        } else {
            chunkSelection.setSecondPoint(chunkMappa);
            text = BukkitTranslationNode.SECOND_POINT_SELECTED;
            soundPitch = 1.0F;
        }

        Text node = text.with(
            "{type}", Texts.getTypeName(Chunk.class),
            "{location}", Chunk.toString(chunkMappa));
        textHandler.send(entity, node);
        XSound.BLOCK_NOTE_BLOCK_BASS.play(entity, 1.0F, soundPitch);
    }
}
