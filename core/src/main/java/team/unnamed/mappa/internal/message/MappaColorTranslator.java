package team.unnamed.mappa.internal.message;

import me.yushust.message.format.MessageInterceptor;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.color.ColorScheme;

import java.util.Map;

public class MappaColorTranslator implements MessageInterceptor {
    private final String prefix;
    private final Map<ColorScheme, String> colors;

    public MappaColorTranslator(String prefix, Map<ColorScheme, String> colors) {
        this.prefix = prefix;
        this.colors = colors;
    }

    public String translate(String message) {
        for (Map.Entry<ColorScheme, String> entry : colors.entrySet()) {
            ColorScheme scheme = entry.getKey();
            String prefix = this.prefix + (scheme.ordinal() + 1);
            String translated = entry.getValue();
            message = message.replace(prefix, translated);
        }
        return message;
    }

    @Override
    public @NotNull String intercept(String message) {
        return translate(message);
    }

    public Map<ColorScheme, String> getColors() {
        return colors;
    }
}
