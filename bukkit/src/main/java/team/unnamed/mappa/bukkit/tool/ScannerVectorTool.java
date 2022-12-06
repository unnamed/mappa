package team.unnamed.mappa.bukkit.tool;

import com.cryptomorin.xseries.XSound;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaPlatform;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.object.Vector;
import team.unnamed.mappa.throwable.FindCastException;
import team.unnamed.mappa.throwable.FindException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.Texts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class ScannerVectorTool extends AbstractBukkitTool {
    public static final int LARGE_LENGTH = 10000;

    private final MappaAPI api;
    private final Cache<String, BiMap<Material, MapProperty>> cacheAlias = CacheBuilder.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .build();
    private final Cache<MapProperty, Material> cacheMarker = CacheBuilder.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .build();

    public ScannerVectorTool(MappaAPI api,
                             RegionRegistry regionRegistry,
                             MappaTextHandler textHandler) {
        super(ToolHandler.SCANNER_VECTOR_TOOL,
            true,
            regionRegistry,
            textHandler,
            null);
        this.api = api;
    }

    @Override
    public void interact(MappaPlayer mappaPlayer, Vector lookingAt, Button button, boolean shift) {
        Player entity = mappaPlayer.cast();
        ItemStack itemInHand = entity.getInventory().getItemInHand();
        String pathToScan = NBTEditor.getString(itemInHand, ToolHandler.SCAN_PATH);
        if (pathToScan == null) {
            mappaPlayer.send(
                BukkitTranslationNode
                    .SCAN_PATH_NOT_FOUND
                    .formalText());
            return;
        }
        String schemeName = NBTEditor.getString(itemInHand, ToolHandler.SCAN_SCHEME);
        if (schemeName == null) {
            mappaPlayer.send(
                BukkitTranslationNode
                    .SCAN_SCHEME_NOT_FOUND
                    .formalText());
            return;
        }

        MappaPlatform bootstrap = api.getPlatform();
        MapScheme scheme = bootstrap.getScheme(schemeName);
        if (scheme == null) {
            mappaPlayer.send(
                TranslationNode
                    .SESSION_NOT_FOUND
                    .withFormal("{id}", schemeName));
            return;
        }

        BiMap<Material, MapProperty> aliases = cacheAlias.getIfPresent(pathToScan);
        if (aliases == null) {
            aliases = HashBiMap.create();
            mappaPlayer.send(BukkitTranslationNode
                .SCAN_CACHE
                .withFormal("{path}", pathToScan));
            MapPropertyTree properties = scheme.getTreeProperties();
            tryScanProperty(pathToScan, properties, aliases);
        }

        if (aliases.isEmpty()) {
            mappaPlayer.send(
                BukkitTranslationNode
                    .NOTHING_TO_SCAN
                    .withFormal("{path}", pathToScan));
            return;
        }

        int radius = NBTEditor.getInt(itemInHand, ToolHandler.SCAN_RADIUS);
        // 0 is not null, but anyway it is invalid.
        if (radius == 0) {
            mappaPlayer.send(
                BukkitTranslationNode
                    .SCAN_RADIUS_NOT_FOUND
                    .formalText());
            return;
        }

        boolean deleteBlock = NBTEditor.getBoolean(itemInHand, ToolHandler.SCAN_DELETE_BLOCK);
        boolean deleteMarker = NBTEditor.getBoolean(itemInHand, ToolHandler.SCAN_DELETE_MARKER);

        MapSession session = mappaPlayer.getMapSession();
        if (session == null) {
            mappaPlayer.send(
                BukkitTranslationNode
                    .NO_SESSION_SELECTED
                    .formalText());
            mappaPlayer.send(
                BukkitTranslationNode
                    .SESSION_SELECT_GUIDE
                    .formalText());
            return;
        }

        World world = entity.getWorld();
        Location location = MappaBukkit.toLocation(world, lookingAt);
        Location first = location.clone().add(radius, radius, radius);
        Location second = location.clone().subtract(radius, radius, radius);

        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());

        int length = maxX - minX;
        length += 1; // Add +1 for the block destination
        length = (int) Math.pow(length, 3); // Pow to be 3D
        mappaPlayer.send(
            BukkitTranslationNode
                .SCAN_START
                .withFormal(
                    "{type}", Texts.getTypeName(Vector.class),
                    "{number}", length));
        if (length > LARGE_LENGTH) {
            mappaPlayer.send(
                BukkitTranslationNode.SCAN_WARNING.formalText());
        }
        Set<MapProperty> consumed = new HashSet<>();
        int count = 0;
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block blockAt = world.getBlockAt(x, y, z);
                    Material type = blockAt.getType();
                    if (type == Material.AIR) {
                        continue;
                    }

                    MapProperty property = aliases.get(type);
                    if (property == null) {
                        continue;
                    }

                    if (consumed.contains(property)) {
                        continue;
                    }

                    Material marker = cacheMarker.getIfPresent(property);
                    if (marker != null) {
                        Block blockUp = blockAt.getRelative(BlockFace.UP);
                        Material typeUp = blockUp.getType();
                        if (typeUp != marker) {
                            continue;
                        }
                    }

                    Vector vector = new Vector(x, y, z);
                    try {
                        String path = pathToScan + "." + property.getName();
                        mappaPlayer.setProperty(path, vector, true);

                        boolean firstAlias = property.isFirstAlias();
                        if (firstAlias) {
                            consumed.add(property);
                        }

                        if (deleteBlock) {
                            blockAt.setType(Material.AIR);
                        }

                        if (deleteMarker) {
                            Block relative = blockAt.getRelative(BlockFace.UP);
                            relative.setType(Material.AIR);
                        }

                        ++count;
                    } catch (ParseException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }

        mappaPlayer.send(
            BukkitTranslationNode
                .SCAN_RESULT
                .withFormal(
                    "{type}", Texts.getTypeName(Vector.class),
                    "{number}", count));
        XSound.UI_BUTTON_CLICK.play(entity, 1.0F, 1.5F);
    }

    public void tryScanProperty(String pathToScan,
                                MapPropertyTree properties,
                                BiMap<Material, MapProperty> aliases) {
        try {
            MapProperty property = properties.find(pathToScan);
            scanProperty(pathToScan, property, aliases);
        } catch (ParseException e) {
            if (e instanceof FindCastException) {
                tryScanAll(pathToScan, properties, aliases);
                return;
            }
            throw new RuntimeException(e);
        }
    }

    public void tryScanAll(String pathToScan,
                           MapPropertyTree properties,
                           BiMap<Material, MapProperty> aliases) {
        try {
            Map<String, Object> all = properties.findAll(pathToScan);
            for (Map.Entry<String, Object> entry : all.entrySet()) {
                Object object = entry.getValue();
                if (!(object instanceof MapProperty)) {
                    continue;
                }

                MapProperty property = (MapProperty) object;
                scanProperty(pathToScan, property, aliases);
            }
        } catch (FindException e) {
            throw new RuntimeException(e);
        }
    }

    public void scanProperty(String pathToScan, MapProperty property, BiMap<Material, MapProperty> aliases) {
        @Nullable String[] arrayAliases = property.getAliases();
        if (arrayAliases == null) {
            return;
        }
        for (String arrayAlias : arrayAliases) {
            if (arrayAlias == null) {
                continue;
            }

            Material material;
            int index = arrayAlias.indexOf(":");
            if (index != -1) {
                material = Material.valueOf(arrayAlias.substring(0, index));

                String markerName = arrayAlias.substring(index + 1);
                Material markerMaterial = Material.valueOf(markerName);
                cacheMarker.put(property, markerMaterial);
            } else {
                material = Material.valueOf(arrayAlias);
            }

            aliases.put(material, property);
        }
        cacheAlias.put(pathToScan, aliases);
    }
}
