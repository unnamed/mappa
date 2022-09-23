package team.unnamed.mappa.bukkit.tool;

import com.cryptomorin.xseries.XSound;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.Texts;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class MirrorVectorTool extends AbstractBukkitTool {
    private final Cache<UUID, Vector> cacheCenter = CacheBuilder.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .build();

    public MirrorVectorTool(RegionRegistry regionRegistry,
                            MappaTextHandler textHandler) {
        super(ToolHandler.MIRROR_VECTOR_TOOL,
            false,
            regionRegistry,
            textHandler,
            Vector.class);
    }

    @Override
    public void interact(Player entity,
                         Vector lookingAt,
                         Button button,
                         boolean shift) {
        UUID uuid = entity.getUniqueId();
        String uniqueId = uuid.toString();
        RegionSelection<Vector> vectorSelection =
            regionRegistry.getOrNewVectorSelection(uniqueId);

        if (shift) {
            int floor = (int) lookingAt.getY();
            lookingAt = lookingAt.mutY(++floor);
        }

        if (button == Button.RIGHT) {
            cacheCenter.put(uuid, lookingAt);
            BukkitTranslationNode node = shift
                ? BukkitTranslationNode.CENTER_POINT_FLOOR_SELECTED
                : BukkitTranslationNode.CENTER_POINT_SELECTED;
            Text text = node.with(
                "{type}", Texts.getTypeName(Vector.class),
                "{location}", Vector.toString(lookingAt));
            textHandler.send(entity, text);
            XSound.BLOCK_NOTE_BLOCK_PLING.play(entity, 1.0F, 0.5F);
        } else {
            Vector center = cacheCenter.getIfPresent(uuid);
            if (center == null) {
                textHandler.send(entity,
                    BukkitTranslationNode.CENTER_POINT_NOT_EXISTS);
                return;
            }

            Vector distance = center.distance(lookingAt);
            Vector secondPoint = center.sub(distance);
            setPoint(entity, true, shift, 0.5F, lookingAt, vectorSelection);
            setPoint(entity, false, shift, 1.0F, secondPoint, vectorSelection);
        }
    }

    public void setPoint(Player entity,
                         boolean first,
                         boolean shift,
                         float pitch,
                         Vector lookingAt,
                         RegionSelection<Vector> selection) {
        BukkitTranslationNode text;
        if (first) {
            text = shift
                ? BukkitTranslationNode.FIRST_POINT_FLOOR_SELECTED
                : BukkitTranslationNode.FIRST_POINT_SELECTED;
            selection.setFirstPoint(lookingAt);
        } else {
            text = shift
                ? BukkitTranslationNode.SECOND_POINT_FLOOR_SELECTED
                : BukkitTranslationNode.SECOND_POINT_SELECTED;
            selection.setSecondPoint(lookingAt);
        }

        Text node = text.with(
            "{type}", Texts.getTypeName(Vector.class),
            "{location}", Vector.toString(lookingAt));
        textHandler.send(entity, node);
        XSound.UI_BUTTON_CLICK.play(entity, 1.0F, pitch);
    }
}
