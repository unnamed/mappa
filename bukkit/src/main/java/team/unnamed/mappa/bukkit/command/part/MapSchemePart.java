package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import team.unnamed.mappa.bukkit.exception.ArgumentTextParseException;
import team.unnamed.mappa.bukkit.text.BukkitTranslationNode;
import team.unnamed.mappa.model.map.scheme.MapScheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapSchemePart implements ArgumentPart {
    private final String name;
    private final Map<String, MapScheme> schemeRegistry;

    public MapSchemePart(String name,
                         Map<String, MapScheme> schemeRegistry) {
        this.name = name;
        this.schemeRegistry = schemeRegistry;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<MapScheme> parseValue(CommandContext context,
                                      ArgumentStack stack,
                                      CommandPart caller)
        throws ArgumentParseException {
        String name = stack.next();
        MapScheme scheme = schemeRegistry.get(name);
        if (scheme == null) {
            throw new ArgumentTextParseException(
                BukkitTranslationNode
                    .SCHEME_NOT_FOUND
                    .withFormal("{id}", name));
        }
        return Collections.singletonList(scheme);
    }

    @Override
    public List<String> getSuggestions(CommandContext commandContext, ArgumentStack stack) {
        if (!stack.hasNext()) {
            return null;
        }

        String next = stack.next();
        List<String> suggestions = new ArrayList<>();
        for (String name : schemeRegistry.keySet()) {
            if (name.startsWith(next)) {
                suggestions.add(name);
            }
        }
        return suggestions;
    }
}
