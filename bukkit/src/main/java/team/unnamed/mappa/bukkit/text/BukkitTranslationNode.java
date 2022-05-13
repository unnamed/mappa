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
        "No sessions active."),
    SESSION_LIST_ENTRY("bukkit",
        "   &8- &6{session_id} &8: &6{session_scheme}"),
    VERSION_PLUGIN("bukkit",
        "Mappa v&6{version} &7by &6{author}"),

    SESSION_ID_SET("bukkit",
        "Map session {old} renamed to {new}"),
    SESSION_WARNING_SET("bukkit",
        "Map session {session_id} switch warning to {session_warning}"),

    SETUP_HEADER("bukkit.setup",
        "&8---------[&6Setup {session_id}&8]---------"),
    DEFINE_PROPERTY("bukkit.setup",
        "&7Define property &6{property}&7:"),
    SETUP_PROPERTY_SET("bukkit.setup",
        "   &8- &7/mappa setup <session id> <value>"),
    SETUP_READY("bukkit.setup",
        "&aSession is ready to be saved!"),
    PROPERTY_SKIP_SETUP("bukkit.setup",
        "&7(Can be skipped with &6/mappa skip-setup&7)"),
    VIEW_PROPERTY_SET_HOVER("bukkit.setup",
        "&7Click to view property information"),
    SETUP_PROPERTY_SET_HOVER("bukkit.setup",
        "&7Click to get command"),

    NO_SETUP("bukkit.setup",
        "Everything is setup now."),
    NO_OPTIONAL_SETUP("bukkit.setup",
        "Cannot skip non-optional properties!"),

    FIRST_POINT_SELECTED("bukkit.region",
        "First &6{type} &7point selected at &6{location}"),
    SECOND_POINT_SELECTED("bukkit.region",
        "Second &6{type} &7point selected at &6{location}"),
    FIRST_YAW_PITCH_SELECTED("bukkit.region",
        "First &6Vector &7yaw & pitch point selected at &6{location}"),
    SECOND_YAW_PITCH_SELECTED("bukkit.region",
        "Second &6Vector &7yaw & pitch point selected at &6{location}"),
    FIRST_POINT_NOT_EXISTS("bukkit.region",
        "&cFirst &6{type} &cpoint doesn't exists to set yaw-pitch!"),
    SECOND_POINT_NOT_EXISTS("bukkit.region",
        "&cSecond &6{type} &cpoint doesn't exists to set yaw-pitch!"),
    TOOL_VECTOR_NAME("bukkit.tool",
        "&6Vector tool"),
    TOOL_PRECISE_VECTOR_NAME("bukkit.tool",
        "&6Precise vector tool"),
    TOOL_YAW_PITCH_NAME("bukkit.tool",
        "&6Yaw & pitch tool"),
    TOOL_CHUNK_NAME("bukkit.tool",
        "&6Chunk tool"),
    TOOL_CUSTOM_NAME("bukkit.tool",
        "&7Custom tool: &6{id}"),

    TOOL_NOT_FOUND("bukkit.tool",
        "Tool {id} not found"),
    TOOL_RECEIVED("bukkit.tool",
        "Tool {id} received"),

    SESSION_ALREADY_EXISTS("bukkit.error",
        "A session with id {id} already exists!"),

    NO_SELECTION("bukkit.error",
        "Selection &6{type} &7not found!"),
    NO_FIRST_SELECTION("bukkit.error",
        "First selection &6{type} &7not found!"),
    NO_SECOND_SELECTION("bukkit.error",
        "Second selection &6{type} &7not found!"),

    ;

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
