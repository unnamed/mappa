package team.unnamed.mappa.yaml.mapper;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.model.map.MapProperty;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.throwable.InvalidFormatException;

import java.io.*;
import java.util.List;
import java.util.Map;

public class YamlMapper implements ObjectMapper {
    protected final Yaml yaml;

    public YamlMapper(BaseConstructor constructor, Representer representer, DumperOptions options) {
        this(new Yaml(constructor, representer, options));
    }

    public YamlMapper(BaseConstructor constructor, Representer representer) {
        this(new Yaml(constructor, representer));
    }

    public YamlMapper(Yaml yaml) {
        this.yaml = yaml;
    }

    public YamlMapper() {
        this(new MappaConstructor(), new Representer());
    }

    @Override
    public Map<String, Object> load(File file) throws InvalidFormatException {
        Map<String, Object> mapped;
        try (FileInputStream input = new FileInputStream(file)) {
            mapped = this.yaml.load(input);
        } catch (FileNotFoundException e) {
            throw new InvalidFormatException("File not found", e);
        } catch (IOException e) {
            throw new InvalidFormatException("IO error", e);
        }

        return mapped;
    }

    @Override
    public void saveTo(File file, Map<String, Object> mapped) throws IOException {
        this.yaml.dump(mapped, new FileWriter(file));
    }
}
