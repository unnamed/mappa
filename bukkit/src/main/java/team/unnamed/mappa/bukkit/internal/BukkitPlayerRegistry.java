package team.unnamed.mappa.bukkit.internal;

import me.fixeddev.commandflow.Namespace;
import me.fixeddev.commandflow.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.internal.player.DefaultPlayerRegistry;
import team.unnamed.mappa.model.MappaPlayer;

public class BukkitPlayerRegistry extends DefaultPlayerRegistry<CommandSender> {
    private final MappaAPI api;

    public BukkitPlayerRegistry(MappaAPI api) {
        super(() -> new MappaConsolePlayer(Bukkit.getConsoleSender(), api));
        this.api = api;
    }

    @Override
    public MappaPlayer get(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return console();
        }

        Player player = (Player) sender;
        return registry.computeIfAbsent(
            player.getUniqueId(), uuid -> wrapPlayer(player));
    }

    @Override
    public MappaPlayer get(Namespace namespace) {
        return get(namespace.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE));
    }

    protected MappaPlayer wrapPlayer(Player player) {
        return new MappaBukkitPlayer(player, api);
    }
}
