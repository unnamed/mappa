package team.unnamed.mappa.bukkit.text;

import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextDefault;
import team.unnamed.mappa.object.TextDefaultNode;
import team.unnamed.mappa.object.TextNode;

public enum BukkitTranslationNode implements TextDefault {
    SESSION_LIST_HEADER("bukkit",
        "Sessions {number}:"),
    SESSION_LIST_EMPTY("bukkit",
        "No sessions from this world."),
    SESSION_LIST_ENTRY("bukkit",
        "{number}. {map_scheme}"),

    SETUP_HEADER("bukkit.setup",
        "---------[Setup {map_name}]---------"),
    PROPERTY_NOT_SET("bukkit.setup",
        "Define property {property}"),
    SETUP_PROPERTY_SET("bukkit.setup",
        "- /mappa setup <session id> <arg>"),

    NO_SETUP("bukkit.setup",
        "Everything is setup now."),

    FIRST_POINT_SELECTED("bukkit.region",
        "First point selected at {location}"),
    SECOND_POINT_SELECTED("bukkit.region",
        "Second point selected at {location}"),
    TOOL_VECTOR_NAME("bukkit.tool",
        "Vector selector"),
    TOOL_CHUNK_NAME("bukkit.tool",
        "Chunk selector"),
    TOOL_CUSTOM_NAME("bukkit.tool",
        "Custom selector: {id}"),

    TOOL_NOT_FOUND("bukkit.tool",
        "Tool {id} not found"),
    TOOL_RECEIVED("bukkit.tool",
        "Tool {id} received"),


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
