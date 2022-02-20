package team.unnamed.mappa.command;

import me.fixeddev.commandflow.annotated.part.PartFactory;
import me.fixeddev.commandflow.annotated.part.PartInjector;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.part.CommandPart;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public interface Commands {
    AtomicReference<PartInjector> INJECTOR = new AtomicReference<>(null);

    static CommandPart ofPart(Object object) {
        PartInjector injector = injector();
        Type type = object.getClass();
        PartFactory factory = Objects.requireNonNull(injector.getFactory(type));
        return factory.createPart(type.getTypeName(), Collections.emptyList());
    }

    static PartInjector injector() {
        return Objects.requireNonNull(INJECTOR.get(), "Command injector is null");
    }

    static void setInjector(@NotNull PartInjector injector) {
        PartInjector global = INJECTOR.get();
        if (global != null) {
            throw new NullPointerException("Command injector already initialised!");
        }

        INJECTOR.set(injector);
    }
}
