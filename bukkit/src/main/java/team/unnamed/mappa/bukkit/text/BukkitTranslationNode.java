package team.unnamed.mappa.bukkit.text;

import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextDefault;
import team.unnamed.mappa.object.TextDefaultNode;
import team.unnamed.mappa.object.TextNode;

public enum BukkitTranslationNode implements TextDefault {
    PREFIX_PLUGIN("bukkit",
        "&8[&6Mappa&8] &7&l>> &7"),

    SESSION_LIST_HEADER("bukkit",
        "Sessions &6{number}&7:"),
    SESSION_LIST_EMPTY("bukkit",
        "No sessions from this world."),
    SESSION_LIST_ENTRY("bukkit",
        "&6{number}. &7{map_scheme}"),

    SETUP_HEADER("bukkit.setup",
        "&8---------[&6Setup {map_name}&8]---------"),
    PROPERTY_NOT_SET("bukkit.setup",
        "&7Define property &6{property}&7:"),
    SETUP_PROPERTY_SET("bukkit.setup",
        "&8- &7/mappa setup <session id> <value>"),
    SETUP_PROPERTY_SET_HOVER("bukkit.setup",
        "&7Click to get command"),

    NO_SETUP("bukkit.setup",
        "Everything is setup now."),

    FIRST_POINT_SELECTED("bukkit.region",
        "First point selected at &6{location}"),
    SECOND_POINT_SELECTED("bukkit.region",
        "Second point selected at &6{location}"),
    TOOL_VECTOR_NAME("bukkit.tool",
        "&6Vector selector"),
    TOOL_CHUNK_NAME("bukkit.tool",
        "&6Chunk selector"),
    TOOL_CUSTOM_NAME("bukkit.tool",
        "&7Custom selector: &6{id}"),

    TOOL_NOT_FOUND("bukkit.tool",
        "Tool {id} not found"),
    TOOL_RECEIVED("bukkit.tool",
        "Tool {id} received"),


    VERSION_PLUGIN("bukkit",
        "Mappa v&6{version} &7by &6{author}");

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
