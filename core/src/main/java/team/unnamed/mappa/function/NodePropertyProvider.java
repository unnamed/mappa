package team.unnamed.mappa.function;

import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.throwable.ParseException;

public interface NodePropertyProvider {

    MapProperty parse(ParseContext context, SchemeNode node) throws ParseException;
}
