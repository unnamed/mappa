package team.unnamed.mappa.bukkit.util;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.Namespace;
import me.fixeddev.commandflow.NamespaceImpl;

import java.util.function.Consumer;

public interface CommandBukkit {

    static void execute(CommandManager commandManager, Consumer<Namespace> modifier, String line) {
        NamespaceImpl namespace = new NamespaceImpl();
        modifier.accept(namespace);

        commandManager.execute(namespace, line);
    }
}
