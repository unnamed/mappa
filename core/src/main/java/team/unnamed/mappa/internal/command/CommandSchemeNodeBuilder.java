package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import team.unnamed.mappa.function.EntityProvider;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;

public interface CommandSchemeNodeBuilder {

    static CommandSchemeNodeBuilder builder(PartInjector injector, MappaTextHandler textHandler, EntityProvider provider) {
        return new CommandSchemeNodeBuilderImpl(injector, textHandler, provider);
    }

    /**
     * Create a root command from all the properties of {@link MapScheme}.
     * @param scheme Map scheme to map into commands
     * @return Root command of map scheme.
     */
    Command fromScheme(MapScheme scheme);

    Command fromProperty(String path, MapProperty property);
}
