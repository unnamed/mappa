package team.unnamed.mappa.model.map.scheme;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;
import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public interface MapSchemeFactory {

    static MapSchemeFactory create(MappaInjector injector) {
        return new MapSchemeFactoryImpl(injector);
    }

    MapScheme from(String schemeName, Map<String, Object> objects) throws ParseException;

    MapProperty resolveNode(@NotNull ParseContext context, @NotNull SchemeNode node) throws ParseException;

    void resolveParseConfig(@NotNull ParseContext context, @NotNull NodeParseConfiguration configuration) throws ParseException;
}
