package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.annotated.part.Key;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

public interface CommandSchemeNodeBuilder {

    static CommandSchemeNodeBuilder builder(Key sessionKey, PartInjector injector, MappaTextHandler textHandler, EventBus eventBus) {
        return new CommandSchemeNodeBuilderImpl(sessionKey, injector, textHandler, eventBus);
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
