package team.unnamed.mappa.function;

import team.unnamed.mappa.model.map.node.SchemeCollection;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.throwable.ParseException;

public interface CollectionPropertyProvider {

    MapProperty parse(ParseContext context, SchemeCollection collection, MapProperty processedProperty) throws ParseException;
}
