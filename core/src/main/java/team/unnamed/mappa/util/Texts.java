package team.unnamed.mappa.util;

import me.fixeddev.commandflow.exception.CommandException;
import net.kyori.text.Component;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.Deserializable;
import team.unnamed.mappa.object.DeserializableList;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
        if (o instanceof Deserializable) {
            Deserializable deserializable = (Deserializable) o;
            return deserializable.deserialize();
        } else {
            return String.valueOf(o);
        }
    }

    // I hate this solution - OcZi
    static List<Text> getActionSetTranslation(String path, MapProperty property, Object newValue) {
        List<Text> texts = new ArrayList<>();
        if (property instanceof MapCollectionProperty) {
            String valueString = toPrettifyString(newValue);
            texts.add(TranslationNode
                .PROPERTY_LIST_ADDED
                .withFormal("{type}", getTypeName(property.getType()),
                    "{name}", path,
                    "{value}", valueString
                ));
        } else {
            if (newValue instanceof DeserializableList) {
                texts = new ArrayList<>();
                texts.add(TranslationNode
                    .PROPERTY_CHANGE_TO
                    .withFormal("{name}", path,
                        "{value}", ""));
                DeserializableList list = (DeserializableList) newValue;
                for (String value : list.deserialize()) {
                    texts.add(TranslationNode
                        .PROPERTY_LIST_ADDED_ENTRY
                        .with("{value}", value));
                }
            } else {
                String valueString = toPrettifyString(newValue);
                texts.add(TranslationNode
                    .PROPERTY_CHANGE_TO
                    .withFormal("{name}", path,
                        "{value}", valueString));
            }
        }
        return texts;
    }
}
