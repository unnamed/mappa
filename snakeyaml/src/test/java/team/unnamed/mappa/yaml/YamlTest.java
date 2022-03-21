package team.unnamed.mappa.yaml;

import me.fixeddev.commandflow.SimpleCommandManager;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.defaults.SubCommandPart;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.internal.injector.BasicMappaModule;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class YamlTest {

    public static void main(String[] args) throws ParseException, IOException {
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

        Map<String, Object> sessions = yamlMapper.loadSessions(scheme, new File("serialized.yml"));
        System.out.println("Sessions:");
        map(sessions);
        System.out.println();

        MapSession resumeSession = scheme.resumeSession("MyTest", sessions);
        System.out.println("Session resume:");
        map(resumeSession.getProperties());

        File result = new File("result.yml");
        result.createNewFile();
        yamlMapper.saveTo(result, resumeSession);

        System.out.println();
        System.out.println("Bootstrap:");
        SimpleCommandManager commandManager = new SimpleCommandManager();
        MappaBootstrap bootstrap = new MappaBootstrap(yamlMapper, factory, commandManager);
        bootstrap.load(file);
        mapCommand(bootstrap.getCommandManager()
                .getCommand("mabedwars")
                .orElseThrow(NullPointerException::new),
            null,
            "-> ");
        System.out.println("end");
    }

    public static void mapCommand(Command command, Command parent, String spaces) {
        System.out.println(spaces + "Command: " + command.getName() + " from " + (parent == null ? "nothing" : parent.getName()));
        CommandPart part = command.getPart();
        if (part instanceof SubCommandPart) {
            SubCommandPart subCommandPart = (SubCommandPart) part;
            for (Command subCommand : subCommandPart.getSubCommandMap().values()) {
                System.out.println(spaces + "SubCommand: " + subCommand.getName());
                mapCommand(subCommand, command, "   " + spaces);
            }
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
