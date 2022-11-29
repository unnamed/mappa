package team.unnamed.mappa.util;

import me.fixeddev.commandflow.exception.CommandException;
import net.kyori.text.Component;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import team.unnamed.mappa.object.config.LineDeserializable;
import team.unnamed.mappa.object.config.LineDeserializableList;

import java.lang.reflect.Type;
import java.util.List;

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

    static String toPrettifyString(Object o) {
        if (o instanceof LineDeserializable) {
            LineDeserializable deserializable = (LineDeserializable) o;
            return deserializable.deserialize();
        } else {
            return String.valueOf(o);
        }
    }

    static String toPrettifyString(Object o, String join) {
        if (o instanceof LineDeserializable) {
            LineDeserializable deserializable = (LineDeserializable) o;
            return deserializable.deserialize();
        } else if (o instanceof LineDeserializableList) {
            LineDeserializableList list = (LineDeserializableList) o;
            List<String> deserialize = list.deserialize();
            return String.join(join, deserialize);
        } else {
            return String.valueOf(o);
        }
    }

    static String spacer(int spaces) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    static String capitalize(String str) {
        StringBuilder builder = new StringBuilder(str);
        builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
        return builder.toString();
    }
}
