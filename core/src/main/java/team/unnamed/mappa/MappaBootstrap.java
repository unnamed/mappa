package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.annotated.part.defaults.DefaultsModule;
import me.fixeddev.commandflow.command.Command;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.command.Commands;
import team.unnamed.mappa.internal.command.MappaPartModule;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;
import team.unnamed.mappa.throwable.ParseException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MappaBootstrap {
    private final SchemeMapper mapper;
    private final MapSchemeFactory factory;
    private final CommandManager commandManager;

    private final Map<String, MapScheme> schemeRegistry = new HashMap<>();
    private final CommandSchemeNodeBuilder commandBuilder = CommandSchemeNodeBuilder.builder();

    private boolean loaded;

    public MappaBootstrap(SchemeMapper mapper,
                          MapSchemeFactory factory,
                          CommandManager commandManager) {
        this.mapper = mapper;
        this.factory = factory;
        this.commandManager = commandManager;
    }

    public void load(File schemeFile) throws ParseException {
        if (loaded) {
            return;
        }

        Map<String, Object> load = mapper.load(schemeFile);
        for (Map.Entry<String, Object> entry : load.entrySet()) {
            String schemeName = entry.getKey();
            Map<String, Object> map = (Map<String, Object>) entry.getValue();
            MapScheme scheme = factory.from(schemeName, map);
            schemeRegistry.put(schemeName, scheme);

            Command rootCommand = createCommandsOfScheme(scheme);
            commandManager.registerCommand(rootCommand);
        }

        this.loaded = true;
    }

    protected Command createCommandsOfScheme(MapScheme scheme) {
       if (!Commands.hasInjector()) {
           PartInjector injector = PartInjector.create();
           injector.install(new DefaultsModule());
           injector.install(new MappaPartModule());

           Commands.setInjector(injector);
       }

        return commandBuilder.fromScheme(scheme);
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public MapScheme getScheme(String name) {
        return schemeRegistry.get(name);
    }

    public Map<String, MapScheme> getSchemeRegistry() {
        return schemeRegistry;
    }
}
