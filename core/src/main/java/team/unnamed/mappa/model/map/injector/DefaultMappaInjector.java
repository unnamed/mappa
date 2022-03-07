package team.unnamed.mappa.model.map.injector;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.function.CollectionPropertyProvider;
import team.unnamed.mappa.function.NodePropertyProvider;
import team.unnamed.mappa.function.ParseConfigurationFunction;
import team.unnamed.mappa.model.map.NodeKey;
import team.unnamed.mappa.model.map.configuration.NodeParseConfiguration;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultMappaInjector implements MappaInjector {
    protected final Map<NodeKey, NodePropertyProvider> nodeParseMap = new HashMap<>();
    protected final Map<Type, CollectionPropertyProvider> collectionParseMap = new HashMap<>();
    protected final Map<Type, ParseConfigurationFunction<?>> configParseMap = new HashMap<>();

    @Override
    public void bindNode(NodeKey key, NodePropertyProvider provider) {
        nodeParseMap.put(key, provider);
    }

    @Override
    public void bindCollection(Type key, CollectionPropertyProvider provider) {
        collectionParseMap.put(key, provider);
    }

    @Override
    public <T extends NodeParseConfiguration> void bindConfiguration(Class<T> clazz, ParseConfigurationFunction<T> provider) {
        configParseMap.put(clazz, provider);
    }

    @Override
    public @NotNull NodePropertyProvider getFactoryNode(NodeKey key) {
        System.out.println("parsing key " + key);
        return Objects.requireNonNull(nodeParseMap.get(key),
            key.getTag() + ":" + key.getType().getTypeName() + " not found");
    }

    @Override
    public @NotNull CollectionPropertyProvider getFactoryCollection(Type type) {
        System.out.println("parsing collection " + type);
        return Objects.requireNonNull(collectionParseMap.get(type),
            type.getTypeName() + " not found");
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T extends NodeParseConfiguration> ParseConfigurationFunction<T> getFactoryConfig(Type type) {
        System.out.println("parsing config " + type);
        return (ParseConfigurationFunction<T>)
            Objects.requireNonNull(configParseMap.get(type),
            type.getTypeName() + " not found");
    }

}
