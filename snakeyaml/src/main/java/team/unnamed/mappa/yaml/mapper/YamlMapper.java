package team.unnamed.mappa.yaml.mapper;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.Deserializable;
import team.unnamed.mappa.object.DeserializableList;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.constructor.MappaConstructor;
import team.unnamed.mappa.yaml.constructor.PlainConstructor;
import team.unnamed.mappa.yaml.constructor.SessionConstructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("unchecked")
public class YamlMapper implements SchemeMapper {
    public static final String YAML_FORMAT = ".yml";

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
        PlainConstructor plainConstructor = new PlainConstructor();
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
    public Map<String, Object> resumeSessions(Map<String, MapScheme> schemeMap,
                                              Set<String> blackList,
                                              File file)
        throws ParseException {
        SessionConstructor sessionConstructor = new SessionConstructor(schemeMap, blackList);
        Yaml yamlSession = new Yaml(sessionConstructor);

        Map<String, Object> mapped;
        try (FileInputStream input = new FileInputStream(file)) {
            mapped = (Map<String, Object>) yamlSession.load(input);
        } catch (FileNotFoundException e) {
            throw new ParseException("File not found", e);
        } catch (IOException e) {
            throw new ParseException("IO error", e);
        }

        return mapped;
    }

    @Override
    public void saveTo(FileWriter writer, MapSession session) {
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
        Map<String, Object> dump = serializeProperties(
            formattedName,
            new LinkedHashMap<>(),
            session.getProperties());
        yaml.dump(dump, writer);
    }

    @Override
    public void serializeTo(FileWriter writer, MapSession session) {
        Map<String, Object> serialize = new LinkedHashMap<>();
        serialize.put(SessionConstructor.SESSION_KEY, session.getSchemeName());
        serialize.put("properties", serializeProperties("",
            new LinkedHashMap<>(),
            session.getProperties()));
        // Redundant, but snakeyaml cannot get the root node...
        serialize.put("id", session.getId());
        if (session.isWarning()) {
            serialize.put("warning", true);
        }

        String id = session.getId();
        Map<String, Object> root = Collections
            .singletonMap(id, serialize);
        yaml.dump(root, writer);
    }

    @Override
    public String getFormatFile() {
        return YAML_FORMAT;
    }

    public Map<String, Object> serializeProperties(String rootNode,
                                                   Map<String, Object> root,
                                                   Map<String, MapProperty> map) {
        for (Map.Entry<String, MapProperty> entry : map.entrySet()) {
            MapProperty property = entry.getValue();
            Object value = unwrapValue(property.getValue());
            if (value == null) {
                continue;
            }

            String key = entry.getKey();
            String plainPath = rootNode.isEmpty() ? key : rootNode + "." + key;
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
