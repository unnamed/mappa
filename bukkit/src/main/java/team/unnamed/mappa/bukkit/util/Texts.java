package team.unnamed.mappa.bukkit.util;

import me.fixeddev.commandflow.exception.CommandException;
import net.kyori.text.Component;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

import java.lang.reflect.Type;

public interface Texts {

    static String toString(Component component) {
        return LegacyComponentSerializer
            .INSTANCE
            .serialize(component);
    }

    static String toString(CommandException e) {
        return toString(e.getMessageComponent());
    }

    static String getTypeName(Type type) {
        return type instanceof Class
            ? ((Class<?>) type).getSimpleName()
            : type.getTypeName();
    }
}
