package team.unnamed.mappa.object;

public enum TranslationNode implements TextDefault {

    SCHEME_LOADED("bootstrap",
        "$4{number} $1scheme(s) loaded"),
    SCHEME_COMMAND_LOADED("bootstrap",
        "New command $4{name} $1with aliases $4{aliases} $1from map scheme $4{scheme_name}"),
    LOAD_SUCCESSFULLY("bootstrap",
        "$4Mappa bootstrap loaded successfully"),
    SESSIONS_LOADED("bootstrap",
        "$4{number} $1session(s) loaded"),
    SESSION_WARNING("bootstrap",
        "$6{session_id} $6has warning tag. Any error with this session could be occasioned by a bad map scheme or property corruption."),
    SESSION_SERIALIZED("bootstrap",
        "$4{session_id} $2session loaded as serialized (Reason: $6{session_reason}$2)"),
    REASON_WARNING("bootstrap.ignored-reason",
        "Warning"),
    REASON_BLACK_LIST("bootstrap.ignored-reason",
        "Blacklist"),
    REASON_DUPLICATE("bootstrap.ignored-reason",
        "Duplicated"),
    SESSIONS_RESUMED("bootstrap",
        "$4{number} $1session(s) resume"),
    LOAD_SESSION_WITH_ID_EXISTS("bootstrap",
        "$6There are another map session with ID $4'{id}'$6!"),
    LOAD_SESSION_ID_CHANGED("bootstrap",
        "Renaming $4{id} $1to $4{new-id}$1..."),
    LOAD_SESSION("bootstrap",
        "Map session $4{id} $1loaded"),
    LOAD_FILE_SOURCE("bootstrap",
        "File source $4{path} $1loaded for map scheme $4{id}"),
    DELETE_SESSION("bootstrap",
        "Map session $4{id} $1deleted"),
    DELETE_CONFIRM_SESSION("bootstrap",
        "$6Are you sure to delete map session $1{id}$6? Everything will be erased!"),
    RESUME_SESSION("bootstrap",
        "Map session $4{id} $1resumed"),
    SESSION_MARK_SAVE("bootstrap",
        "Map session $4{id} $1has been marked to be saved when save-all is called"),
    NO_SESSIONS_TO_LOAD("bootstrap",
        "There is no map session to load."),
    NO_SESSIONS_TO_RESUME("bootstrap",
        "There is no map session to resume."),
    SERIALIZE_SESSION("bootstrap",
        "Map session $4{session_id} $1serialized"),
    CANNOT_SERIALIZE_SESSION("bootstrap",
        "Map session $4{session_id} $1is not ready to be saved!"),
    SAVED_SESSION("bootstrap",
        "Map session $4{session_id} $1saved"),
    SAVED_FINISHED("bootstrap",
        "Saved finished."),
    UNLOAD_SCHEMES("bootstrap",
        "Unload Map scheme registry"),
    UNLOAD_MAP_SESSIONS("bootstrap",
        "Unload Map sessions"),
    UNLOAD_COMMANDS("bootstrap",
        "Unload commands"),

    SESSION_OR_SERIALIZED_NOT_FOUND("bootstrap.error",
        "$6Map session or serialized session $4{id} $6not found"),
    SESSION_NOT_FOUND("bootstrap.error",
        "$6Map session $4{id} $6not found"),
    SESSION_IS_SERIALIZED("bootstrap.error",
        "Map session $4{session_id} $1is serialized! (Reason: $4{session_reason}$1)"),
    SERIALIZED_SESSION_NOT_FOUND("bootstrap.error",
        "$6Map serialized session $4{id} $6not found"),
    SCHEME_NOT_FOUND("bootstrap.error",
        "$6Map scheme $4{id} $6not found"),

    SESSION_ALREADY_EXISTS("bootstrap.error",
        "$6Map session with id $4{id} $6already exists!"),

    NEW_SESSION("bootstrap.session",
        "Map session $4{session_id} $1using scheme $4{session_scheme} $1created"),
    SELECTED_SESSION("bootstrap.session",
        "Map session $4{session_id} $1selected"),
    DESELECTED_SESSION("bootstrap.session",
        "Map session $4{session_id} $1deselected"),
    VERIFY_SESSION_SUCCESS("bootstrap.session",
        "Verification of map session $4{session_id} $1finished without errors"),
    VERIFY_SESSION_FAIL("bootstrap.session",
        "Verification fails with $4{number} $1error(s):"),
    VERIFY_SESSION_FAIL_ENTRY("bootstrap.session",
        "$2- $4{property} $2-> $6{error}"),
    VERIFY_SESSION_FAIL_SHORTCUT("bootstrap.session",
        "$2- $6and $4{number} $6more..."),

    PROPERTY_CHANGE_TO("parse.info",
        "Property $4{name} $1set to $4{value}"),
    PROPERTY_INFO_HEADER("parse.info",
        "$2---------[$4Property {name}$2]---------"),
    PROPERTY_INFO_PATH("parse.info",
        "$1Path: $4{path}"),
    PROPERTY_INFO_TYPE("parse.info",
        "$1Type: $4{type}"),
    PROPERTY_INFO_VALUE("parse.info",
        "$1Value: $4{value}"),
    PROPERTY_INFO_VALUE_LIST("parse.info",
        "   $2- $4{value}"),
    PROPERTY_CLEAR("parse.info",
        "Property $4{name} $1has been cleared"),
    PROPERTY_LIST_ADDED("parse.info",
        "Property {type} $4{name} $1added: $4{value}"),
    PROPERTY_LIST_ADDED_ENTRY("parse.info",
        "   $2- $4{value}"),
    PROPERTY_LIST_REMOVED("parse.info",
        "Property {type} $4{name} $1removed $4{value}"),
    PROPERTY_LIST_VALUE_NOT_FOUND("parse.info",
        "Value $4{value} $1not found in property list $4{name}"),
    SESSION_INFO_HEADER("parse.info",
        "$2---------[$4Map session {session_id}$2]---------"),
    SESSION_MAP_NAME("parse.info",
        "Name: $4{session_map_name}"),
    SESSION_WORLD_NAME("parse.info",
        "World: $4{session_world_name}"),
    SESSION_DATE("parse.info",
        "Date: $4{session_date}"),
    SESSION_VERSION("parse.info",
        "Version: $4{session_version}"),
    SESSION_AUTHOR("parse.info",
        "Author(s): $4{author}"),
    SESSION_AUTHOR_ENTRY("parse.info",
        "- $4{author}"),

    PARENT_CONFIG_NOT_FOUND("parse.error",
        "Invalid type of parameter {parameter}, require: {type}"),

    PROPERTY_READ_ONLY("parse.error",
        "Property is read-only (trying to set a map scheme's property?)"),
    INVALID_TYPE("parse.error",
        "Invalid type of parameter {name}: {parameter}, require: {type}"),
    NUMBER_NON_POSITIVE("parse.error",
        "Number {number} is not positive"),
    NUMBER_NON_NEGATIVE("parse.error",
        "Number {number} is not negative"),

    FLAG_CONFLICT("parse.error",
        "Flag key {key} conflicts with {conflict}"),
    FLAG_DUPLICATION("parse.error",
        "Flag key {key} duplication in parse"),
    METADATA_NO_NAME("parse.error",
        "Metadata {path} does not have a name"),
    CLONE_PATH_NOT_FOUND("parse.error",
        "Trying to clone path {path} found null"),
    UNDEFINED_PROPERTY("parse.error",
        "$4{property} $6is undefined"),
    INVALID_PROPERTY("parse.error",
        "Invalid property {property}"),

    ERROR_MESSAGE("parse.error",
        "$4{property} $2-> $6{text}"),

    ;

    private final String path;
    private final TextDefaultNode textNode;

    TranslationNode(String path, String message) {
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
