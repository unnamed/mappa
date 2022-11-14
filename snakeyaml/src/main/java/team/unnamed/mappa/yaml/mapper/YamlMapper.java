package team.unnamed.mappa.yaml.mapper;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.config.LineDeserializable;
import team.unnamed.mappa.object.config.LineDeserializableList;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.constructor.MappaConstructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("unchecked")
public class YamlMapper implements SchemeMapper {
    public static final String YAML_FORMAT = "yml";

    protected final Map<File, Map<String, Object>> cacheYaml = new HashMap<>();

    protected final Yaml yaml;

    protected final BaseConstructor constructor;

    public static SchemeMapper newMapper() {
        return new YamlMapper();
    }

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
    public Map<String, Object> load(File file) throws ParseException {
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
            throw new ParseException("File not found " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new ParseException("IO error", e);
        }

        return mapped;
    }

    @Override
    public Map<String, Object> loadSessions(MapScheme scheme, File file) throws ParseException {
        Yaml yaml = new Yaml();

        Map<String, Object> mapped;
        try (FileInputStream input = new FileInputStream(file)) {
            mapped = (Map<String, Object>) yaml.load(input);
        } catch (FileNotFoundException e) {
            throw new ParseException("File not found", e);
        } catch (IOException e) {
            throw new ParseException("IO error", e);
        }

        return mapped;
    }

    @Override
    public void saveTo(File file, MapEditSession session) {
        MapScheme scheme = session.getScheme();
        String formattedName = scheme.getFormatName();
        String mapName = session.getMapName();
        if (mapName != null) {
            formattedName = formattedName
                .replace("{map_name}", mapName);
        }
        String version = session.getVersion();
        if (version != null) {
            formattedName = formattedName
                .replace("{map_version}", version);
        }
        String worldName = session.getWorldName();
        if (worldName != null) {
            formattedName = formattedName
                .replace("{world_name}", worldName);
        }
        String schemeName = session.getSchemeName();
        if (schemeName != null) {
            formattedName = formattedName
                .replace("{scheme_name}", schemeName);
        }
        Map<String, Object> dump = serializeProperties(session.getRawProperties());
        String finalFormattedName = formattedName;
        cacheYaml.compute(file, (fileKey, map) -> {
            if (map == null) {
                map = cacheFile(fileKey);
            }

            map.put(finalFormattedName, dump);
            return map;
        });
    }

    public Map<String, Object> cacheFile(File file) {
        Map<String, Object> mapped;
        try (FileInputStream input = new FileInputStream(file)) {
            mapped = (Map<String, Object>) this.yaml.load(input);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeException("IO error", e);
        }

        if (mapped == null) {
            mapped = new LinkedHashMap<>();
        }
        return mapped;
    }

    @Override
    public void applySave(File file) throws IOException {
        Map<String, Object> root = cacheYaml.get(file);
        if (root == null) {
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(root, writer);
        }

        cacheYaml.remove(file);
    }

    @Override
    public String getFormatFile() {
        return YAML_FORMAT;
    }

    public Map<String, Object> serializeProperties(Map<String, Object> map) {
        Map<String, Object> root = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object object = entry.getValue();
            String key = entry.getKey();
            if (object instanceof Map) {
                Map<String, Object> otherMap = (Map<String, Object>) object;
                Map<String, Object> serialize = serializeProperties(otherMap);
                if (serialize.isEmpty()) {
                    continue;
                }

                root.put(key, serialize);
            } else if (object instanceof MapProperty) {
                MapProperty property = (MapProperty) object;
                if (property.isIgnore()) {
                    continue;
                }

                Object value = property.getValue();
                if (value == null) {
                    continue;
                }

                Object serialized = unwrapValue(value);
                if (serialized == null) {
                    continue;
                }

                root.put(key, serialized);
            }
        }

        return root;
    }

    private Object unwrapValue(Object value) {
        if (value instanceof Collection) {
            List<Object> valueList = new ArrayList<>((Collection<?>) value);
            ListIterator<Object> it = valueList.listIterator();
            while (it.hasNext()) {
                Object next = it.next();
                it.set(unwrapValue(next));
            }
            return valueList;
        } else if (value instanceof LineDeserializable) {
            value = ((LineDeserializable) value).deserialize();
        } else if (value instanceof LineDeserializableList) {
            value = ((LineDeserializableList) value).deserialize();
        }
        return value;
    }
}
