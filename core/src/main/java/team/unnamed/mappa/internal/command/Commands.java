package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.annotated.part.Module;
import me.fixeddev.commandflow.annotated.part.PartFactory;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.part.CommandPart;
import team.unnamed.mappa.model.map.property.MapProperty;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;

public interface Commands {

    static CommandPart ofPart(PartInjector injector, MapProperty property) {
        return ofPart(injector, property.getType());
    }

    static CommandPart ofPart(PartInjector injector, Type type) {
        PartFactory factory = Objects.requireNonNull(
            injector.getFactory(type),
            "Part type " + type.getTypeName() + " not found");
        String name;
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            name = clazz.getSimpleName();
        } else {
            name = type.getTypeName();
        }
        return factory.createPart(name, Collections.emptyList());
    }

    static PartInjector newInjector(Module... modules) {
        PartInjector partInjector = PartInjector.create();
        for (Module module : modules) {
            partInjector.install(module);
        }
        return partInjector;
    }
}
