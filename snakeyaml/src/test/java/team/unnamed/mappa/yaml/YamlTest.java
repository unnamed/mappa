package team.unnamed.mappa.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.throwable.InvalidFormatException;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class YamlTest {

    public static void main(String[] args) throws InvalidFormatException {
        DumperOptions options = new DumperOptions();
        File file = new File("schemes.yml");
        YamlMapper yamlMapper = new YamlMapper(new MappaConstructor(), new Representer(), options);
        Map<?, ?> load = yamlMapper.load(file);
        System.out.println("load = " + load);
        map(load);
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
                collection.forEach(object -> {
                    System.out.println("- " + object + ", type: " + ((object != null ? object.getClass() : null)));
                });
            }
        }
    }

}
