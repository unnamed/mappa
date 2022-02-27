package team.unnamed.mappa.yaml.mapper;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.throwable.InvalidFormatException;
import team.unnamed.mappa.yaml.MappaConstructor;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class YamlMapper implements ObjectMapper {
    protected final Yaml yaml;
    protected final BaseConstructor constructor;

    public YamlMapper(BaseConstructor constructor, Representer representer, DumperOptions options) {
        this.yaml = new Yaml(constructor, representer, options);
        this.constructor = constructor;
    }

    public YamlMapper(BaseConstructor constructor, Representer representer) {
        this(constructor, representer, new DumperOptions());
    }

    public YamlMapper() {
        this(new MappaConstructor(), new Representer());
    }

    @Override
    public Map<String, Object> load(File file) throws InvalidFormatException {
        Map<String, Object> mapped;
        try (FileInputStream input = new FileInputStream(file)) {
            if (constructor instanceof MappaConstructor) {
                Path path = Paths.get(file.toURI());
                // Load from another input stream to avoid this input finish before yaml can read them
                byte[] bufferBytes = Files.readAllBytes(path);
                String buffer = new String(bufferBytes);
                MappaConstructor mappaConstructor = (MappaConstructor) this.constructor;
                mappaConstructor.setBuffer(buffer);
            }
            mapped = (Map<String, Object>) this.yaml.load(input);
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
