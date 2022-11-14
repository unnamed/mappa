package team.unnamed.mappa.bukkit.internal;

import org.bukkit.command.ConsoleCommandSender;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.model.AbstractMappaPlayer;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.Vector;

import java.util.UUID;

public class MappaConsolePlayer extends AbstractMappaPlayer<ConsoleCommandSender> {

    public MappaConsolePlayer(ConsoleCommandSender entity, MappaAPI api) {
        super(entity, api);
    }

    @Override
    public void send(String message) {
        entity.sendMessage(message);
    }

    @Override
    public void send(Text text) {
        String message = textHandler.formatLang("en", text);
        if (text.isFormal()) {
            message = textHandler.getPrefix("en") + message;
        }
        send(message);
    }

    @Override
    protected void sendActionSessionList(String line) {
        send(line);
    }

    @Override
    protected void sendActionSetup(Text defineText,
                                   Text typeText,
                                   String argLine,
                                   String arg,
                                   MapProperty property) {
        send(defineText);
        send(typeText);

        send(BukkitTranslationNode
            .SETUP_PROPERTY_SET
            .with("{arg}", arg));

        if (property.isOptional()) {
            send(" ");
            send(BukkitTranslationNode.PROPERTY_SKIP_SETUP.text());
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public Vector getPosition(boolean block) {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public String getWorldName() {
        return null;
    }
}
