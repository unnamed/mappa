package team.unnamed.mappa.bukkit.text;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.TextDefault;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Another file configuration class from other personal projects.
 * Used to create a refillable configuration.
 * @author _OcZi
 */
public class YamlFile extends YamlConfiguration {
    @Nullable
    private final JavaPlugin plugin;
    @Nullable
    private final List<TextDefault> refill;

    @NotNull
    private final String fileName;
    @NotNull
    private final File file;

    public YamlFile(@NotNull JavaPlugin plugin,
                    @NotNull String fileName) {
        this(plugin, fileName, null);
    }

    public YamlFile(@NotNull JavaPlugin plugin,
                    @NotNull String fileName,
                    @Nullable List<TextDefault> refill) {
        this(plugin,
            new File(plugin.getDataFolder(), fileName),
            refill);
    }

    public YamlFile(@Nullable JavaPlugin plugin,
                    @NotNull File file) {
        this(plugin, file, null);
    }

    public YamlFile(@Nullable JavaPlugin plugin,
                    @NotNull File file,
                    @Nullable List<TextDefault> refill) {
        this.plugin = plugin;
        this.refill = refill;
        String name = file.getName();
        checkState(!name.isEmpty(), "Name of YamlFile is empty!");
        if (!name.endsWith(".yml")) {
            String uri = file.getAbsolutePath() + ".yml";
            name += ".yml";
            this.file = new File(uri);
        } else {
            this.file = file;
        }
        this.fileName = name;
        saveDefault();
        loadFileConfiguration();

        refillNodes();
    }

    public static void refillFileWith(@NotNull JavaPlugin plugin,
                                      @NotNull String fileName,
                                      @Nullable List<TextDefault> refill) {
        // Will be refilled with refillNodes() in constructor init
        new YamlFile(plugin, fileName, refill);
    }

    public void loadFileConfiguration() {
        try {
            load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void refillNodes() {
        if (refill == null) {
            return;
        }
        for (TextDefault node : refill) {
            if (isSet(node.getNode())) continue;
            set(node.getNode(), node.getDefaultMessage());
        }
        save();
    }

    public void save() {
        try {
            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveDefault() {
        if (!file.exists()) {
            try {
                if (plugin != null && plugin.getResource(fileName) != null) {
                    plugin.saveResource(fileName, false);
                } else {
                    if (plugin != null) {
                        plugin.getDataFolder().mkdirs();
                    }
                    file.createNewFile();
                }
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get file of {@link YamlConfiguration}.
     *
     * @return File of YamlFile.
     */
    @NotNull
    public File getFile() {
        return file;
    }

    /**
     * Get file name of {@link YamlConfiguration}.
     *
     * @return File name of YamlFile.
     */
    @NotNull
    public String getFileName() {
        return fileName;
    }
}
