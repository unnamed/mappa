package team.unnamed.mappa.function;

import me.fixeddev.commandflow.CommandContext;

public interface EntityProvider {

    Object fromContext(CommandContext context);
}
