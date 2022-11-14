package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaPlatform;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MapSessionPart implements ArgumentPart {
    protected final String name;
    protected final MappaPlatform platform;

    public MapSessionPart(String name, MappaPlatform platform) {
        this.name = name;
        this.platform = platform;
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
        MapSession session = platform.getMapSessionById(next);
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
        Collection<MapSession> sessions = platform.getMapRegistry().getMapSessions();
        for (MapSession session : sessions) {
            String id = session.getId();
            if (id.startsWith(next)) {
                suggestions.add(id);
            }
        }
        return suggestions;
    }
}
