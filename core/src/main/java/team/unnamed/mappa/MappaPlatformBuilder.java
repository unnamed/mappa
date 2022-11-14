package team.unnamed.mappa;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.command.MappaCommandManager;
import team.unnamed.mappa.internal.mapper.SchemeMapper;
import team.unnamed.mappa.model.map.scheme.MapSchemeFactory;

import java.io.File;

public interface MappaPlatformBuilder {

    MappaPlatformBuilder mapper(@NotNull SchemeMapper mapper);

    MappaPlatformBuilder dataFolder(@NotNull File dataFolder);

    MappaPlatformBuilder mapSchemeFactory(@NotNull MapSchemeFactory mapSchemeFactory);

    MappaPlatformBuilder commandManager(@NotNull MappaCommandManager commandManager);

    MappaPlatformBuilder commandManager(@NotNull CommandManager commandManager, @NotNull PartInjector injector);

    boolean checkSchemeMapper();

    boolean checkDataFolder();

    boolean checkMapSchemeFactory();

    boolean checkCommandManager();

    MappaPlatform build();
}
