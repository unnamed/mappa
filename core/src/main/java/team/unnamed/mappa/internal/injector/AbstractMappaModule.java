package team.unnamed.mappa.internal.injector;

import team.unnamed.mappa.function.CollectionPropertyProvider;
import team.unnamed.mappa.function.NodePropertyProvider;
import team.unnamed.mappa.function.ParseConfigurationFunction;
import team.unnamed.mappa.model.map.NodeKey;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;

import java.lang.reflect.Type;

public abstract class AbstractMappaModule implements MappaModule {
    protected MappaInjector injector;

    @Override
    public void bindNode(NodeKey key, NodePropertyProvider function) {
        injector.bindNode(key, function);
    }

    @Override
    public void bindCollection(Type key, CollectionPropertyProvider function) {
        injector.bindCollection(key, function);
    }

    @Override
    public <T extends NodeParseConfiguration> void bindConfiguration(Class<T> clazz, ParseConfigurationFunction<T> function) {
        injector.bindConfiguration(clazz, function);
    }

    @Override
    public abstract void configure();

    @Override
    public void setInjector(MappaInjector injector) {
        this.injector = injector;
    }
}
