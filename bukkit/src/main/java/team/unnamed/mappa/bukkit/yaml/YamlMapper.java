package team.unnamed.mappa.bukkit.yaml;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

public class YamlMapper extends FileConfiguration {
    protected final DumperOptions options = new DumperOptions();
    protected final YamlRepresenter representer = new YamlRepresenter();
    protected final Yaml yaml = new Yaml(new YamlConstructor(), representer, options);

    @Override
    public String saveToString() {
        return null;
    }

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        Map<?, ?> input;
        try {
            input = (Map<?, ?>) this.yaml.load(contents);
        } catch (YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        /*
        String header = this.parseHeader(contents);
        if (header.length() > 0) {
            this.options().header(header);
        }

        if (input != null) {
            this.convertMapsToSections(input, this);
        }*/

    }

    @Override
    protected String buildHeader() {
        return "";
    }
}
