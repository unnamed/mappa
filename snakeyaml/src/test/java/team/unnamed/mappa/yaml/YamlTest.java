package team.unnamed.mappa.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.internal.injector.BasicMappaModule;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.throwable.InvalidFormatException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class YamlTest {

    public static void main(String[] args) throws InvalidFormatException, ParseException {
        DumperOptions options = new DumperOptions();
        File file = new File("schemes.yml");
        Representer representer = new Representer();
        YamlMapper yamlMapper = new YamlMapper(new MappaConstructor(), representer, options);
        Map<String, Object> load = yamlMapper.load(file);
        System.out.println("Load:");
        map(load);

        MappaInjector injector = MappaInjector.newInjector(new BasicMappaModule());
        MapSchemeFactory factory = MapScheme.factory(injector);
        MapScheme scheme = factory.from("mabedwars", (Map<String, Object>) load.get("MABedwars"));
        System.out.println("Scheme:");
        map(scheme.getProperties());
        System.out.println();

        Yaml yaml = new Yaml(new PlainConstructor(true));
        try (FileInputStream input = new FileInputStream("serialized.yml")) {
            Map<String, Object> maps = (Map<String, Object>) yaml.load(input);
            System.out.println("Maps:");
            map(maps);
            System.out.println();

            Map<String, Object> myTest = (Map<String, Object>) maps.get("MyTest");
            MapSession resumeSession = scheme.resumeSession("MyTest", myTest);
            System.out.println("Session resume:");
            map(resumeSession.getProperties());

            System.out.println("end");
            File result = new File("result.yml");
            result.createNewFile();
            yamlMapper.saveTo(result, resumeSession);
        } catch (FileNotFoundException e) {
            throw new InvalidFormatException("File not found", e);
        } catch (IOException e) {
            throw new InvalidFormatException("IO error", e);
        }
    }

    public static void map(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println("key: " + entry.getKey());
            Object value = entry.getValue();
            System.out.println("value: " + value);
            if (value == null) {
                continue;
            }
            System.out.println("type: " + value.getClass());
            if (value instanceof Map) {
                map((Map<?, ?>) value);
            } else if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                collection.forEach(object ->
                    System.out.println("- " + object + ", type: " + ((object != null ? object.getClass() : null))));
            }
        }
    }

}
