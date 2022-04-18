package team.unnamed.mappa.bukkit.util;

import me.fixeddev.commandflow.exception.CommandException;
import net.kyori.text.Component;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public interface Texts {

    static String toString(Component component) {
        return LegacyComponentSerializer
            .INSTANCE
            .serialize(component);
    }

    static String toString(CommandException e) {
        return toString(e.getMessageComponent());
    }
}
