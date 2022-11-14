package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.command.CommandSchemeNodeBuilder;
import team.unnamed.mappa.internal.command.DefaultMappaCommandManager;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;

import java.io.File;

public class MappaPlatformImplBuilder implements MappaPlatformBuilder {
    private final MappaAPI api;
    private SchemeMapper mapper;
    private MapSchemeFactory schemeFactory;
    private File dataFolder;
    private MappaCommandManager commandManager;

    public MappaPlatformImplBuilder(MappaAPI api) {
        this.api = api;
    }

    @Override
    public MappaPlatformBuilder mapper(@NotNull SchemeMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    @Override
    public MappaPlatformBuilder dataFolder(@NotNull File dataFolder) {
        this.dataFolder = dataFolder;
        return this;
    }

    @Override
    public MappaPlatformBuilder mapSchemeFactory(@NotNull MapSchemeFactory mapSchemeFactory) {
        this.schemeFactory = mapSchemeFactory;
        return this;
    }

    @Override
    public MappaPlatformBuilder commandManager(@NotNull MappaCommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    @Override
    public MappaPlatformBuilder commandManager(@NotNull CommandManager commandManager, @NotNull PartInjector injector) {
        return commandManager(new DefaultMappaCommandManager(commandManager,
            CommandSchemeNodeBuilder.builder(injector, api), api.getPlayerRegistry()));
    }

    @Override
    public boolean checkSchemeMapper() {
        return mapper != null;
    }

    @Override
    public boolean checkDataFolder() {
        return dataFolder != null;
    }

    @Override
    public boolean checkMapSchemeFactory() {
        return schemeFactory != null;
    }

    @Override
    public boolean checkCommandManager() {
        return commandManager != null;
    }

    @Override
    public MappaPlatform build() {
        return new MappaPlatformImpl(api,
            mapper,
            schemeFactory,
            dataFolder,
            commandManager);
    }
}
