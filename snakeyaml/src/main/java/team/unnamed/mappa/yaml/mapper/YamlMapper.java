package team.unnamed.mappa.yaml.mapper;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.configuration.InterpretMode;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.Deserializable;
import team.unnamed.mappa.object.DeserializableList;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.MappaConstructor;
import team.unnamed.mappa.yaml.PlainConstructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("unchecked")
public class YamlMapper implements SchemeMapper {
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
            throw new ParseException("File not found", e);
        } catch (IOException e) {
            throw new ParseException("IO error", e);
        }

        return mapped;
    }

    @Override
    public Map<String, Object> loadSessions(MapScheme scheme, File file) throws ParseException {
        PlainConstructor plainConstructor = new PlainConstructor(
            scheme.getInterpretMode() == InterpretMode.NODE_PER_MAP);
        Yaml yamlPlain = new Yaml(plainConstructor);

        Map<String, Object> mapped;
        try (FileInputStream input = new FileInputStream(file)) {
            mapped = (Map<String, Object>) yamlPlain.load(input);
        } catch (FileNotFoundException e) {
            throw new ParseException("File not found", e);
        } catch (IOException e) {
            throw new ParseException("IO error", e);
        }

        return mapped;
    }

    @Override
    public void saveTo(File folder, MapSession session) throws IOException {
        MapScheme scheme = session.getScheme();
        Map<String, Object> dump = serializeProperties(
            scheme.getInterpretMode() == InterpretMode.NODE_PER_MAP
                ? session.getWorldName()
                : "",
            new LinkedHashMap<>(),
            session.getProperties());
        yaml.dump(dump, new FileWriter(folder));
    }

    public Map<String, Object> serializeProperties(String worldName,
                                                   Map<String, Object> root,
                                                   Map<String, MapProperty> map) {
        for (Map.Entry<String, MapProperty> entry : map.entrySet()) {
            MapProperty property = entry.getValue();
            Object value = unwrapValue(property.getValue());
            if (value == null) {
                continue;
            }

            String key = entry.getKey();
            String plainPath = worldName.isEmpty() ? key : worldName + "." + key;
            String[] path = plainPath.split("\\.");
            Map<String, Object> mapPath = processPath(path, root);

            String valuePath = path[path.length - 1];
            mapPath.put(valuePath, value);
        }

        return root;
    }

    private Object unwrapValue(Object value) {
        if (value instanceof List) {
            List<Object> valueList = new ArrayList<>((List<?>) value);
            ListIterator<Object> it = valueList.listIterator();
            while (it.hasNext()) {
                Object next = it.next();
                it.set(unwrapValue(next));
            }
            return valueList;
        } else if (value instanceof Deserializable) {
            value = ((Deserializable) value).deserialize();
        } else if (value instanceof DeserializableList) {
            value = ((DeserializableList) value).deserialize();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> processPath(String[] path, Map<String, Object> root) {
        if (path.length < 2) {
            return root;
        }
        Map<String, Object> mapPath = null;
        for (int i = 0; i < path.length - 1; i++) {
            if (mapPath != null) {
                mapPath = (Map<String, Object>)
                    mapPath.computeIfAbsent(path[i], key -> new LinkedHashMap<>());
            } else {
                mapPath = (Map<String, Object>)
                    root.computeIfAbsent(path[i], key -> new LinkedHashMap<>());
            }
        }
        return mapPath;
    }

}
