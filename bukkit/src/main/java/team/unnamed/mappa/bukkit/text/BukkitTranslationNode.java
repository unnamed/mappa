package team.unnamed.mappa.bukkit.text;

import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextDefault;
import team.unnamed.mappa.object.TextDefaultNode;
import team.unnamed.mappa.object.TextNode;

public enum BukkitTranslationNode implements TextDefault {
    PREFIX_PLUGIN("bukkit",
        "$2[$4Mappa$2] $1&l>> "),

    SESSION_LIST_HEADER("bukkit",
        "Map session(s) $2[$4{number}$2]$1:"),
    SESSION_LIST_EMPTY("bukkit",
        "No map sessions active."),
    SESSION_LIST_ENTRY("bukkit",
        "   $2- $4{session_id} $2: $4{session_scheme}"),
    SESSION_LIST_ENTRY_SELECT("bukkit",
        "$1Select $4{id} $1map session"),
    SESSION_LIST_ENTRY_RESOLVE("bukkit",
        "$1Resolve $4{id} $1map session"),
    SESSION_LIST_ENTRY_REASON("bukkit",
        "$1Serialized reason: $4{session_reason}"),
    VERSION_PLUGIN("bukkit",
        "Mappa v$4{version} $1by $4{author}"),

    SESSION_ID_SET("bukkit",
        "Map session $4{old} $1renamed to $4{new}"),
    SESSION_WARNING_SET("bukkit",
        "Map session $4{session_id} $1switch warning to $4{session_warning}"),

    SETUP_HEADER("bukkit.setup",
        "$2---------[$4Setup {session_id}$2]---------"),
    DEFINE_PROPERTY("bukkit.setup",
        "$1Define property $4{property}$1:"),
    TYPE_PROPERTY("bukkit.setup",
        "$1Type: $4{type}"),
    SETUP_PROPERTY_SET("bukkit.setup",
        "   $2- $4/mappa setup {arg}"),
    SETUP_READY("bukkit.setup",
        "$3Session is ready to be saved!"),
    PROPERTY_SKIP_SETUP("bukkit.setup",
        "$1(Can be skipped with $4/mappa skip-setup$1)"),
    VIEW_PROPERTY_SET_HOVER("bukkit.setup",
        "$1Click to view property information"),
    SETUP_PROPERTY_SET_HOVER("bukkit.setup",
        "$1Click to get command"),
    SETUP_ACTION_BAR("bukkit.setup",
        "$1Property to define: $4{property}"),

    NO_SETUP("bukkit.setup",
        "Everything is setup now."),
    NO_OPTIONAL_SETUP("bukkit.setup",
        "Cannot skip non-optional properties!"),

    SHOW_VISUAL("bukkit.visual",
        "Visual enabled of $4{property}"),
    HIDE_VISUAL("bukkit.visual",
        "Visual disabled of $4{property}"),
    NO_VISUAL("bukkit.visual",
        "$6Property {property} doesn't have any effect!"),
    CLEAR_VISUAL("bukkit.visual",
        "All previous visuals has been disabled."),

    CLIPBOARD_COPIED("bukkit.copy",
        "$5Copied to clipboard"),
    CLIPBOARD_PASTED("bukkit.copy",
        "$5Clipboard pasted"),
    CLIPBOARD_MIRROR_PASTED("bukkit.copy",
        "$5Clipboard mirrored pasted"),
    CLIPBOARD_CAST_PASTED("bukkit.copy",
        "$5Clipboard cast to {new-path} and pasted"),
    NO_CLIPBOARD("bukkit.copy",
        "$5No path copied yet!"),
    NOTHING_TO_COPY("bukkit.copy",
        "$5No cloneable properties found in this path"),

    FIRST_POINT_SELECTED("bukkit.region",
        "$5#1 {type} point selected at {location}"),
    SECOND_POINT_SELECTED("bukkit.region",
        "$5#2 {type} point selected at {location}"),
    FIRST_POINT_FLOOR_SELECTED("bukkit.region",
        "$5#1 {type} point selected at floor {location}"),
    SECOND_POINT_FLOOR_SELECTED("bukkit.region",
        "$5#2 {type} point selected at floor {location}"),
    FIRST_YAW_PITCH_SELECTED("bukkit.region",
        "$5#1 Vector yaw & pitch point selected at {location}"),
    SECOND_YAW_PITCH_SELECTED("bukkit.region",
        "$5#2 Vector yaw & pitch point selected at {location}"),
    CENTER_POINT_SELECTED("bukkit.region",
        "$5Center {type} point selected at {location}"),
    CENTER_POINT_FLOOR_SELECTED("bukkit.region",
        "$5Center {type} point selected at floor {location}"),
    TOOL_VECTOR_NAME("bukkit.tool",
        "$4Vector tool"),
    TOOL_CENTERED_VECTOR_NAME("bukkit.tool",
        "$4Centered vector tool"),
    TOOL_PRECISE_VECTOR_NAME("bukkit.tool",
        "$4Precise vector tool"),
    TOOL_MIRROR_VECTOR_NAME("bukkit.tool",
        "$4Mirror vector tool"),
    TOOL_YAW_PITCH_NAME("bukkit.tool",
        "$4Yaw & pitch tool"),
    TOOL_CENTERED_YAW_PITCH_NAME("bukkit.tool",
        "$4Centered yaw & pitch tool"),
    TOOL_CHUNK_NAME("bukkit.tool",
        "$4Chunk tool"),
    TOOL_ARMOR_STAND_NAME("bukkit.tool",
        "$4Armor stand tool"),
    TOOL_REGION_RADIUS("bukkit.tool",
        "$4Region radius tool"),
    TOOL_REGION_RADIUS_LORE("bukkit.tool",
        "$1Radius: $4{radius}"),
    TOOL_CUSTOM_REGION_RADIUS("bukkit.tool",
        "$4Custom region radius tool"),
    TOOL_CUSTOM_REGION_RADIUS_X("bukkit.tool",
        "$1X: $4{radius}"),
    TOOL_CUSTOM_REGION_RADIUS_Y_PLUS("bukkit.tool",
        "$1Y Plus: $4{radius}"),
    TOOL_CUSTOM_REGION_RADIUS_Y_MINUS("bukkit.tool",
        "$1Y Minus: $4{radius}"),
    TOOL_CUSTOM_REGION_RADIUS_Z("bukkit.tool",
        "$1Z: $4{radius}"),
    TOOL_SCANNER_VECTOR_NAME("bukkit.tool",
        "$4Scanner vector tool"),
    TOOL_SCANNER_VECTOR_LORE_SCHEME("bukkit.tool",
        "$1Scheme: $4{scheme}"),
    TOOL_SCANNER_VECTOR_LORE_PATH("bukkit.tool",
        "$1Path: $4{path}"),
    TOOL_SCANNER_VECTOR_LORE_RADIUS("bukkit.tool",
        "$1Radius: $4{radius}"),
    TOOL_CUSTOM_NAME("bukkit.tool",
        "$1Custom tool: $4{id}"),

    TOOL_NOT_FOUND("bukkit.tool",
        "Tool $4{id} $1not found"),
    TOOL_RECEIVED("bukkit.tool",
        "Tool $4{id} $1received"),

    SCAN_CACHE("bukkit.scan",
        "Creating scan cache in $4{path}..."),
    NOTHING_TO_SCAN("bukkit.scan",
        "Path $4{path} $1doesn't have properties to scan!"),
    TOOL_SCANNING("bukkit.scan",
        "Scanning $4{path}..."),

    SCAN_START("bukkit.scan",
        "{type} Scanning $4{number} $1blocks.."),
    SCAN_WARNING("bukkit.scan",
        "$6This is a heavy task to process. The main thread can be locked!"),
    SCAN_RESULT("bukkit.scan",
        "{type} Scanner ends with $4{number} $1changes."),

    RADIUS_AXIS_NON_NEGATIVE("bukkit.error",
        "Any radius cannot be negative!"),
    SCAN_PATH_NOT_FOUND("bukkit.error",
        "Tool doesn't have path to scan!"),
    SCAN_SCHEME_NOT_FOUND("bukkit.error",
        "Tool doesn't have map scheme to scan!"),
    SCAN_RADIUS_NOT_FOUND("bukkit.error",
        "Tool doesn't have radius to scan!"),
    NO_SESSION_SELECTED("bukkit.error",
        "There is not a map session selected!"),
    SESSION_SELECT_GUIDE("bukkit.error",
        "Use $4/mappa select <session id> $1to select a map session"),

    SESSION_ALREADY_EXISTS("bukkit.error",
        "A session with id {id} already exists!"),

    FIRST_POINT_NOT_EXISTS("bukkit.error",
        "$6#1 {type} point doesn't exists to set yaw-pitch!"),
    SECOND_POINT_NOT_EXISTS("bukkit.error",
        "$6#2 {type} point doesn't exists to set yaw-pitch!"),
    CENTER_POINT_NOT_EXISTS("bukkit.error",
        "$6Center point doesn't exist!"),
    NO_SELECTION("bukkit.error",
        "Selection $4{type} $1not found!"),
    NO_FIRST_SELECTION("bukkit.error",
        "First selection $4{type} $1not found!"),
    NO_SECOND_SELECTION("bukkit.error",
        "Second selection $4{type} $1not found!"),

    ;

    private final String path;
    private final TextDefaultNode textNode;

    BukkitTranslationNode(String path, String message) {
        this.path = path;
        this.textNode = new TextDefaultNode(getPath(), message);
    }

    @Override
    public TextNode text() {
        return Text.with(getPath());
    }

    @Override
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
