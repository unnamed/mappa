package team.unnamed.mappa.bukkit.internal;

import me.fixeddev.commandflow.bukkit.BukkitDefaultTranslationProvider;
import team.unnamed.mappa.object.TextDefault;
import team.unnamed.mappa.object.TextDefaultNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GettableTranslationProvider extends BukkitDefaultTranslationProvider {

    public List<TextDefault> toTexts(String parentNode) {
        List<TextDefault> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            TextDefaultNode node = new TextDefaultNode(parentNode + entry.getKey(), entry.getValue());
            list.add(node);
        }
        return list;
    }

    public Map<String, String> getTranslations() {
        return this.translations;
    }
}
