package team.unnamed.mappa.function;

import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.throwable.ParseException;

public interface ParseConfigurationFunction<T extends NodeParseConfiguration> {

    void configure(ParseContext context, T config) throws ParseException;
}
