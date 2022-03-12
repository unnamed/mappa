package team.unnamed.mappa.internal.injector;

import team.unnamed.mappa.function.CollectionPropertyProvider;
import team.unnamed.mappa.function.NodePropertyProvider;
import team.unnamed.mappa.function.ParseConfigurationFunction;
import team.unnamed.mappa.model.map.NodeKey;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;
import team.unnamed.mappa.throwable.ParseException;

import java.lang.reflect.Type;

public interface MappaModule {

    default void bindNode(Type key, NodePropertyProvider function) {
        bindNode(null, key, function);
    }

    default void bindNode(String tag, Type key, NodePropertyProvider function) {
        bindNode(new NodeKey(tag, key), function);
    }

    void bindNode(NodeKey key, NodePropertyProvider function);

    void bindCollection(Type key, CollectionPropertyProvider function);

    <T extends NodeParseConfiguration> void bindConfiguration(Class<T> clazz, ParseConfigurationFunction<T> function);

    void configure() throws ParseException;

    void setInjector(MappaInjector injector);
}
