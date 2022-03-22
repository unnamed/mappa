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
        PartFactory factory = Objects.requireNonNull(injector.getFactory(type));
        return factory.createPart(type.getTypeName(), Collections.emptyList());
    }

    static PartInjector newInjector(Module... modules) {
        PartInjector partInjector = PartInjector.create();
        for (Module module : modules) {
            partInjector.install(module);
        }
        return partInjector;
    }
}
