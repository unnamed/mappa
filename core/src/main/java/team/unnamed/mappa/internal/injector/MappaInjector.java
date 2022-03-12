package team.unnamed.mappa.internal.injector;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.function.CollectionPropertyProvider;
import team.unnamed.mappa.function.NodePropertyProvider;
import team.unnamed.mappa.function.ParseConfigurationFunction;
import team.unnamed.mappa.model.map.NodeKey;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;
import team.unnamed.mappa.throwable.ParseException;

import java.lang.reflect.Type;

public interface MappaInjector {

    static MappaInjector newInjector() {
        return new DefaultMappaInjector();
    }

    static MappaInjector newInjector(MappaModule... modules) throws ParseException {
        MappaInjector injector = newInjector();
        for (MappaModule module : modules) {
            injector.install(module);
        }
        return injector;
    }

    default void bindNode(Type key, NodePropertyProvider function) {
        bindNode(null, key, function);
    }

    default void bindNode(String tag, Type key, NodePropertyProvider function) {
        bindNode(new NodeKey(tag, key), function);
    }

    void bindNode(NodeKey key, NodePropertyProvider function);

    void bindCollection(Type key, CollectionPropertyProvider function);

    <T extends NodeParseConfiguration> void bindConfiguration(Class<T> clazz, ParseConfigurationFunction<T> function);

    @NotNull
    default NodePropertyProvider getFactoryNode(Type key) {
        return getFactoryNode(null, key);
    }

    @NotNull
    default NodePropertyProvider getFactoryNode(String tag, Type key) {
        return getFactoryNode(new NodeKey(tag, key));
    }

    @NotNull
    NodePropertyProvider getFactoryNode(NodeKey key);

    @NotNull
    CollectionPropertyProvider getFactoryCollection(Type key);

    @NotNull
    default <T extends NodeParseConfiguration> ParseConfigurationFunction<T> getFactoryConfig(Class<T> clazz) {
        return getFactoryConfig((Type) clazz);
    }

    <T extends NodeParseConfiguration> ParseConfigurationFunction<T> getFactoryConfig(Type type);

    default void install(MappaModule module) throws ParseException {
        module.setInjector(this);
        module.configure();
        module.setInjector(null);
    }
}
