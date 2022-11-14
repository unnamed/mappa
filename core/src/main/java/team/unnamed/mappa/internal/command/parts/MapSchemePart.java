package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import team.unnamed.mappa.internal.MapRegistry;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapSchemePart implements ArgumentPart {
    private final String name;
    private final MapRegistry registry;

    public MapSchemePart(String name,
                         MapRegistry registry) {
        this.name = name;
        this.registry = registry;
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
        MapScheme scheme = registry.getMapScheme(name);
        if (scheme == null) {
            throw new ArgumentTextParseException(
                TranslationNode
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
        for (MapScheme scheme : registry.getMapSchemes()) {
            String name = scheme.getName();
            if (name.startsWith(next)) {
                suggestions.add(name);
            }
        }
        return suggestions;
    }
}
