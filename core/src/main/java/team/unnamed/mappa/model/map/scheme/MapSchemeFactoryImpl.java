package team.unnamed.mappa.model.map.scheme;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.function.CollectionPropertyProvider;
import team.unnamed.mappa.function.NodePropertyProvider;
import team.unnamed.mappa.function.ParseConfigurationFunction;
import team.unnamed.mappa.internal.injector.MappaInjector;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;
import team.unnamed.mappa.model.map.node.SchemeCollection;
import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.model.map.property.MapNodeProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.ParseException;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapSchemeFactoryImpl implements MapSchemeFactory {
    private final MappaInjector injector;

    public MapSchemeFactoryImpl(MappaInjector injector) {
        this.injector = injector;
    }

    @Override
    public MapScheme from(String name, Map<String, Object> mapped) throws ParseException {
        Map<String, MapProperty> properties = new LinkedHashMap<>();
        ParseContext context = new ParseContext(name, mapped, properties);
        mapScheme("", context, mapped, properties);
        return new DefaultMapScheme(context);
    }

    @SuppressWarnings("unchecked")
    public void mapScheme(String currentPath,
                          ParseContext context,
                          Map<String, Object> mapped,
                          Map<String, MapProperty> properties) throws ParseException {
        for (Map.Entry<String, Object> entry : mapped.entrySet()) {
            String path = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                String mapPath = resolvePath(currentPath, path);
                context.setCurrentNode(null);
                context.setCurrentPath(mapPath);
                mapScheme(mapPath, context, (Map<String, Object>) value, properties);
            } else if (value instanceof SchemeNode) {
                SchemeNode node = (SchemeNode) value;
                String name = node.getName();
                String propertyPath = resolvePath(currentPath, name);
                propertyPath = propertyPath.replace("?", "");
                context.setCurrentNode(node);
                context.setCurrentPath(propertyPath);
                MapProperty property = resolveNode(context, node);
                properties.put(propertyPath, property);
            } else if (value instanceof NodeParseConfiguration) {
                context.setCurrentNode(null);
                context.setCurrentPath(currentPath);
                resolveParseConfig(context, (NodeParseConfiguration) value);
            }
        }
    }

    private String resolvePath(String path, String add) {
        return path.isEmpty() ? add : path + "." + add;
    }

    @Override
    public MapProperty resolveNode(@NotNull ParseContext context, @NotNull SchemeNode node) throws ParseException {
        if (!(node instanceof SchemeCollection)) {
            return resolveSchemeNode(context, node);
        }

        SchemeCollection collection = (SchemeCollection) node;
        CollectionPropertyProvider provider = injector.getFactoryCollection(collection.getCollectionType());
        MapProperty property = resolveSchemeNode(context, collection.getTypeNode());
        return provider.parse(context, collection, (MapNodeProperty) property);
    }

    private MapProperty resolveSchemeNode(ParseContext context, SchemeNode node) throws ParseException {
        NodePropertyProvider provider = injector.getFactoryNode(node.toKey());
        return provider.parse(context, node);
    }

    @Override
    public void resolveParseConfig(@NotNull ParseContext context, @NotNull NodeParseConfiguration configuration) throws ParseException {
        Type type = configuration.getClass();
        ParseConfigurationFunction<NodeParseConfiguration> function = injector.getFactoryConfig(type);
        function.configure(context, configuration);
    }
}
