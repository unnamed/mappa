package team.unnamed.mappa.bukkit.internal;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import team.unnamed.mappa.MappaAPI;
import team.unnamed.mappa.bukkit.util.MappaBukkit;
import team.unnamed.mappa.model.AbstractMappaPlayer;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.BukkitTranslationNode;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.Vector;

import java.util.UUID;

public class MappaBukkitPlayer extends AbstractMappaPlayer<Player> {

    public MappaBukkitPlayer(Player entity, MappaAPI api) {
        super(entity, api);
    }

    @Override
    protected void sendActionSessionList(String line) {
        Player player = cast();
        TextComponent component = new TextComponent(line);
        String id = session.getId();
        if (session instanceof MapEditSession) {
            ClickEvent clickEvent = new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/mappa select " + id);
            component.setClickEvent(clickEvent);

            String hover = format(
                BukkitTranslationNode
                    .SESSION_LIST_ENTRY_SELECT
                    .with("{id}", id));
            TextComponent hoverComponent = new TextComponent(hover);
            HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hoverComponent});
            component.setHoverEvent(hoverEvent);
        }

        player.spigot().sendMessage(component);
    }

    @Override
    protected void sendActionSetup(Text defineText,
                                   Text typeText,
                                   String argLine,
                                   String arg,
                                   MapProperty property) {
        Player.Spigot spigot = this.<Player>cast().spigot();
        spigot.sendMessage(commandComponent(
            defineText,
            BukkitTranslationNode.VIEW_PROPERTY_SET_HOVER.formalText(),
            ClickEvent.Action.RUN_COMMAND,
            argLine + " -v "));

        send(typeText);

        String command = "mappa setup";
        if (!arg.isEmpty()) {
            command += " ";
        }
        spigot.sendMessage(
            commandComponent(
                BukkitTranslationNode
                    .SETUP_PROPERTY_SET
                    .with("{arg}", arg),
                command));

        if (property.isOptional()) {
            send(" ");
            spigot.sendMessage(
                commandComponent(
                    BukkitTranslationNode
                        .PROPERTY_SKIP_SETUP
                        .text(),
                    "mappa skip-setup"));
        }
    }

    public TextComponent commandComponent(Text message,
                                          Text hoverText,
                                          ClickEvent.Action clickAction,
                                          String command) {
        TextComponent component = new TextComponent(
            format(message));

        ClickEvent clickEvent = new ClickEvent(clickAction, "/" + command);
        component.setClickEvent(clickEvent);

        String hover = format(hoverText);
        TextComponent hoverComponent = new TextComponent(hover);
        HoverEvent hoverEvent = new HoverEvent(
            HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hoverComponent});
        component.setHoverEvent(hoverEvent);
        return component;
    }

    public TextComponent commandComponent(Text message, Text hoverText, String command) {
        return commandComponent(
            message,
            hoverText,
            ClickEvent.Action.SUGGEST_COMMAND,
            command);
    }

    public TextComponent commandComponent(Text message, String command) {
        return commandComponent(
            message,
            BukkitTranslationNode.SETUP_PROPERTY_SET_HOVER.formalText(),
            command);
    }

    @Override
    public void send(String message) {
        entity.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return entity.hasPermission(permission);
    }

    public Location getLocation() {
        return entity.getLocation();
    }

    @Override
    public Vector getPosition(boolean block) {
        Location loc = getLocation();
        return block ? MappaBukkit.toMappaBlock(loc) : MappaBukkit.toMappa(loc);
    }

    @Override
    public UUID getUniqueId() {
        return entity.getUniqueId();
    }

    @Override
    public String getWorldName() {
        return entity.getWorld().getName();
    }
}
