package team.unnamed.mappa.bukkit.text;

import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextDefault;
import team.unnamed.mappa.object.TextDefaultNode;
import team.unnamed.mappa.object.TextNode;

public enum BukkitTranslationNode implements TextDefault {
    SESSION_LIST_HEADER("bukkit",
        "Sessions {number}:"),
    SESSION_LIST_ENTRY("bukkit",
        "{number}. {map_scheme}"),

    VERSION_PLUGIN("bukkit",
        "Mappa v{version} by {author}");

    private final String path;
    private final TextDefaultNode textNode;

    BukkitTranslationNode(String path, String message) {
        this.path = path;
        this.textNode = new TextDefaultNode(getPath(), message);
    }

    public TextNode text() {
        return Text.with(getPath());
    }

    public TextNode formalText() {
        return Text.withFormal(getPath());
    }

    @Override
    public String getDefaultMessage() {
        return textNode.getDefaultMessage();
    }

    public String getName() {
        return name().toLowerCase();
    }

    public String getPath() {
        return path + "." + getName();
    }

    public String getNode() {
        return textNode.getNode();
    }

    @Override
    public Object[] getPlaceholders() {
        return null;
    }

    @Override
    public boolean isFormal() {
        return false;
    }
}
