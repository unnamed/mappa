package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapSessionPart implements ArgumentPart {
    private final String name;
    protected final MappaBootstrap bootstrap;

    public MapSessionPart(String name, MappaBootstrap bootstrap) {
        this.name = name;
        this.bootstrap = bootstrap;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<MapSession> parseValue(CommandContext context,
                                       ArgumentStack stack,
                                       @Nullable CommandPart caller)
        throws ArgumentParseException {
        String next = stack.next();
        MapSession session = bootstrap.getSessionById(next);
        if (session == null) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SESSION_NOT_FOUND
                    .withFormal("{id}", next));
        }
        return Collections.singletonList(session);
    }

    @Override
    public List<String> getSuggestions(CommandContext commandContext, ArgumentStack stack) {
        if (!stack.hasNext()) {
            return null;
        }

        String next = stack.next();
        List<String> suggestions = new ArrayList<>();
        Map<String, MapSession> sessions = bootstrap.getSessionMap();
        for (String id : sessions.keySet()) {
            if (id.startsWith(next)) {
                suggestions.add(id);
            }
        }
        return suggestions;
    }
}
