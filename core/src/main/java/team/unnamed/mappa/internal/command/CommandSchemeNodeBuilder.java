package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

public interface CommandSchemeNodeBuilder {

    static CommandSchemeNodeBuilder builder(PartInjector injector,
                                            MappaAPI api) {
        return new CommandSchemeNodeBuilderImpl(
            injector,
            api.getTextHandler(),
            api.getEventBus(),
            api.getVisualizer());
    }


    PartInjector getInjector();

    /**
     * Map all scheme tree to commands.
     * @param scheme Map scheme to map.
     * @return Root command of map scheme.
     */
    Command fromScheme(MapScheme scheme) throws ParseException;

    /**
     * Map the property to command.
     * @param path Path for permissions and reference.
     * @param property Property to map.
     * @return Property as command.
     */
    Command fromProperty(String path, MapProperty property);
}
