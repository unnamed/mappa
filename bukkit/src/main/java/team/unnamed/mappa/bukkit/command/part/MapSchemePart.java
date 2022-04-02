package team.unnamed.mappa.bukkit.command.part;

import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.model.map.scheme.MapScheme;

import java.util.Map;

public class MapSchemePart implements CommandPart {
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
    public void parse(CommandContext context,
                      ArgumentStack stack,
                      @Nullable CommandPart part) throws ArgumentParseException {
        String name = stack.next();
        MapScheme scheme = schemeRegistry.get(name);
        if (scheme == null) {
            throw new ArgumentParseException();
        }

        context.setValue(this, scheme);
    }
}
