package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.MappaBootstrap;
import team.unnamed.mappa.model.map.MapSerializedSession;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ArgumentTextParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapSerializedSessionPart extends MapSessionPart {

    public MapSerializedSessionPart(String name, MappaBootstrap bootstrap) {
        super(name, bootstrap);
    }

    @Override
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart caller)
        throws ArgumentParseException {
        String id = stack.next();
        MapSession serialized = bootstrap.getSessionById(id);
        if (serialized == null) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SESSION_NOT_FOUND
                    .withFormal("{id}", id));
        } else if (!(serialized instanceof MapSerializedSession)) {
            throw new ArgumentTextParseException(
                TranslationNode
                    .SERIALIZED_SESSION_NOT_FOUND
                    .withFormal("{id}", id));
        }

        context.setValue(this, serialized);
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
            if (!(session instanceof MapSerializedSession)) {
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
