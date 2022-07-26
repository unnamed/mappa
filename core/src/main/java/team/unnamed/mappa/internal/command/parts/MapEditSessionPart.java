package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapEditSessionPart extends MapSessionPart {

    public MapEditSessionPart(String name, MappaBootstrap bootstrap) {
        super(name, bootstrap);
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
        } else if (session instanceof MapSerializedSession) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SESSION_IS_SERIALIZED
                    .formalText(),
                session);
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
        for (Map.Entry<String, MapSession> entry : sessions.entrySet()) {
            MapSession session = entry.getValue();
            if (!(session instanceof MapEditSession)) {
                continue;
            }

            String id = entry.getKey();
            if (id.startsWith(next)) {
                suggestions.add(id);
            }
        }
        return suggestions;
    }
}
