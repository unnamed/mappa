package team.unnamed.mappa.yaml;

import me.fixeddev.commandflow.SimpleCommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.part.defaults.SubCommandPart;
import me.yushust.message.MessageHandler;
import me.yushust.message.source.properties.PropertiesFileSource;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.command.MappaPartModule;
import team.unnamed.mappa.internal.injector.BasicMappaModule;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.throwable.ParseRuntimeException;
import team.unnamed.mappa.yaml.constructor.MappaConstructor;
import team.unnamed.mappa.yaml.mapper.YamlMapper;

import java.io.*;
import java.util.Map;
import java.util.Properties;

import static team.unnamed.mappa.internal.mapper.SchemeMapper.printMap;

public class YamlTest implements MappaAPI {
    MappaBootstrap bootstrap;

    public static void main(String[] args) throws ParseException, IOException {
        DumperOptions options = new DumperOptions();
        File file = new File("schemes.yml");
        Representer representer = new Representer();
        YamlMapper yamlMapper = new YamlMapper(new MappaConstructor(), representer, options);
        Map<String, Object> load = yamlMapper.load(file);
        System.out.println("Load:");
        printMap(load);


        MappaInjector injector = MappaInjector.newInjector(new BasicMappaModule());
        MapSchemeFactory factory = MapScheme.factory(injector);
        MapScheme scheme = factory.from("mabedwars", (Map<String, Object>) load.get("MABedwars"));
        System.out.println("Scheme:");
        printMap(scheme.getTreeProperties().getRawMaps());
        System.out.println();

        Map<String, Object> sessions = yamlMapper.loadSessions(
            scheme,
            new File("serialized.yml"));
        System.out.println("Sessions:");
        printMap(sessions);
        System.out.println();

        File folder = new File(System.getProperty("user.home"));
        // Creating and refill translations
        File langUs = new File(folder, "lang_en_US.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(langUs));
        for (TranslationNode value : TranslationNode.values()) {
            Object node = properties.get(value.getNode());
            if (node == null) {
                properties.put(value.getNode(), value.getDefaultMessage());
            }
        }
        properties.store(new FileOutputStream(langUs), null);
        MessageHandler handler = MessageHandler.of(
            new PropertiesFileSource(
                folder,
                "lang_%lang%.properties"),
            handle -> handle.specify(PrintStream.class)
                .setMessageSender((out, mode, message) -> out.println(message))
                .setLinguist(out -> "en_US")
        );
        MappaTextHandler textHandler = new MappaTextHandler(handler, context -> System.out, null);
        System.out.println("Session resume:");
        try {
            Map<String, Object> myTest = (Map<String, Object>) sessions.get("MyTest");
            System.out.println("My test:");
            printMap(myTest);
            MapEditSession resumeSession = scheme.resumeSession("MyTest", myTest);
            printMap(resumeSession.getProperties().getRawMaps());

            File result = new File("result.yml");
            result.createNewFile();
            yamlMapper.saveTo(result, resumeSession);
        } catch (ParseException e) {
            textHandler.send(System.out, e.getTextNode());
            throw e;
        } catch (ParseRuntimeException e) {
            textHandler.send(System.out, e.getTextNode());
            throw e;
        }

        System.out.println();
        System.out.println("Bootstrap:");
        SimpleCommandManager commandManager = new SimpleCommandManager();
        YamlTest api = new YamlTest();
        PartInjector partInjector = Commands.newInjector(
            new DefaultsModule(),
            new MappaPartModule(api)
        );

        MappaBootstrap bootstrap = new MappaBootstrap(yamlMapper,
            factory,
            new File(""),
            commandManager,
            partInjector,
            textHandler);
        api.bootstrap = bootstrap;
        bootstrap.loadSchemes(file, System.out);
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

    @Override
    public MappaBootstrap getBootstrap() {
        return null;
    }

    @Override
    public @Nullable RegionRegistry getRegionRegistry() {
        return null;
    }

    @Override
    public @Nullable ToolHandler getToolHandler() {
        return null;
    }
}
