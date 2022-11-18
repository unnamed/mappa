package team.unnamed.mappa.bukkit.command;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.annotation.Command;
import me.fixeddev.commandflow.annotated.annotation.OptArg;
import me.fixeddev.commandflow.annotated.annotation.SubCommandClasses;
import me.fixeddev.commandflow.annotated.annotation.Switch;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import team.unnamed.mappa.bukkit.MappaPlugin;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.command.*;
import team.unnamed.mappa.internal.command.parts.Sender;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.region.RegionSelection;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.util.Texts;

import java.util.List;
import java.util.Set;

@Command(
    names = {"mappa", "map"},
    permission = "mappa.command"
)
@SubCommandClasses({
    ClipboardCommand.class,
    MapSessionCommand.class,
    SetupCommand.class,
    ToolCommand.class})
public class MappaCommand extends HelpCommand {
    private final MappaPlugin plugin;

    public MappaCommand(MappaPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(names = {"help", "?"})
    public void onHelp(MappaPlayer sender, @OptArg("1") int page, CommandContext context) {
        help(sender, page, context);
    }

    @Command(names = {"vector-pos-1", "vpos1"})
    public void setFirstVector(@Sender MappaPlayer player,
                               @Switch("looking-at") boolean lookingAt) {
        setVectorSelection(player, true, lookingAt);
    }

    @Command(names = {"vector-pos-2", "vpos2"})
    public void setSecondVector(@Sender MappaPlayer player,
                                @Switch("looking-at") boolean lookingAt) {
        setVectorSelection(player, false, lookingAt);
    }

    public void setVectorSelection(MappaPlayer player, boolean first, boolean lookingAt) {
        RegionRegistry regionRegistry = plugin.getRegionRegistry();
        String uuid = player.getUniqueId().toString();
        RegionSelection<Vector> selection =
            regionRegistry.getOrNewVectorSelection(uuid);

        Vector newVector;
        Player bukkit = player.cast();
        if (lookingAt) {
            Block targetBlock = bukkit.getTargetBlock((Set<Material>) null, 5);
            if (targetBlock.isEmpty()) {
                BukkitTranslationNode node = first
                    ? BukkitTranslationNode.NO_FIRST_SELECTION
                    : BukkitTranslationNode.NO_SECOND_SELECTION;
                player.send(
                    node.withFormal(
                        "{type}", Texts.getTypeName(Vector.class)));
                return;
            }
            newVector = MappaBukkit.toMappaVector(targetBlock);
        } else {
            Location location = bukkit.getLocation();
            newVector = MappaBukkit.toMappa(location);
        }

        String typeName = Texts.getTypeName(Vector.class);
        BukkitTranslationNode node;
        if (first) {
            selection.setFirstPoint(newVector);
            node = BukkitTranslationNode.FIRST_POINT_SELECTED;
        } else {
            selection.setSecondPoint(newVector);
            node = BukkitTranslationNode.SECOND_POINT_SELECTED;
        }

        player.send(
            node
                .with("{type}", typeName,
                    "{location}", Vector.toString(newVector)));
    }

    @Command(names = {"version", "v"})
    public void showVersion(MappaPlayer sender) {
        PluginDescriptionFile description = plugin.getDescription();
        List<String> authors = description.getAuthors();
        String version = description.getVersion();
        sender.send(
            BukkitTranslationNode
                .PLUGIN_VERSION
                .withFormal(
                    "{version}", version,
                    "{author}", String.join(",", authors)
                )
        );
        sender.send(
            BukkitTranslationNode
                .PLUGIN_URL
                .withFormal("{url}", "https://github.com/unnamed/mappa")
        );
    }
}
