package team.unnamed.mappa.object;

public enum TranslationNode implements TextDefault {

    SCHEME_LOADED("bootstrap",
        "&6{number} &7scheme(s) loaded"),
    SCHEME_COMMAND_LOADED("bootstrap",
        "New command &6{name} &7with aliases &6{aliases} &7from map scheme &6{scheme_name}"),
    LOAD_SUCCESSFULLY("bootstrap",
        "Mappa bootstrap loaded successfully"),
    SESSIONS_LOADED("bootstrap",
        "&6{number} &7session(s) loaded"),
    SESSION_WARNING("bootstrap",
        "&6{session_id} &chas warning tag. Any error with this session could be occasioned by a bad map scheme or property corruption."),
    SESSION_SERIALIZED("bootstrap",
        "&6{session_id} &8session loaded as serialized (Reason: &6{session_reason}&8)"),
    REASON_WARNING("bootstrap.ignored-reason",
        "Warning"),
    REASON_BLACK_LIST("bootstrap.ignored-reason",
        "Blacklist"),
    REASON_DUPLICATE("bootstrap.ignored-reason",
        "Duplicated"),
    SESSIONS_RESUMED("bootstrap",
        "&6{number} &7session(s) resume"),
    LOAD_SESSION_WITH_ID_EXISTS("bootstrap",
        "&cThere are another map session with ID &6'{id}'&c!"),
    LOAD_SESSION_ID_CHANGED("bootstrap",
        "Renaming &6{id} &7to &6{new-id}&7..."),
    LOAD_SESSION("bootstrap",
        "Map session &6{id} &7loaded"),
    LOAD_FILE_SOURCE("bootstrap",
        "File source &6{path} &7loaded for map scheme &6{id}"),
    DELETE_SESSION("bootstrap",
        "Map session &6{id} &7deleted"),
    DELETE_CONFIRM_SESSION("bootstrap",
        "&cAre you sure to delete map session &7{id}&c? Everything will be erased!"),
    RESUME_SESSION("bootstrap",
        "Map session &6{id} &7resumed"),
    SESSION_MARK_SAVE("bootstrap",
        "Map session &6{id} &7has been marked to be saved when save-all is called"),
    NO_SESSIONS_TO_LOAD("bootstrap",
        "There is no map session to load."),
    NO_SESSIONS_TO_RESUME("bootstrap",
        "There is no map session to resume."),
    SERIALIZE_SESSION("bootstrap",
        "Map session &6{session_id} &7serialized"),
    CANNOT_SERIALIZE_SESSION("bootstrap",
        "Map session &6{session_id} &7is not ready to be saved!"),
    SAVED_SESSION("bootstrap",
        "Map session &6{session_id} &7saved"),
    SAVED_FINISHED("bootstrap",
        "Saved finished."),
    UNLOAD_SCHEMES("bootstrap",
        "Unload Map scheme registry"),
    UNLOAD_MAP_SESSIONS("bootstrap",
        "Unload Map sessions"),
    UNLOAD_COMMANDS("bootstrap",
        "Unload commands"),

    SESSION_OR_SERIALIZED_NOT_FOUND("bootstrap.error",
        "&cMap session or serialized session &6{id} &cnot found"),
    SESSION_NOT_FOUND("bootstrap.error",
        "&cMap session &6{id} &cnot found"),
    SESSION_IS_SERIALIZED("bootstrap.error",
        "Map session &6{session_id} &7is serialized! (Reason: &6{session_reason}&7)"),
    SERIALIZED_SESSION_NOT_FOUND("bootstrap.error",
        "&cMap serialized session &6{id} &cnot found"),
    SCHEME_NOT_FOUND("bootstrap.error",
        "&cMap scheme &6{id} &cnot found"),

    SESSION_ALREADY_EXISTS("bootstrap.error",
        "&cMap session with id &6{id} &calready exists!"),

    NEW_SESSION("bootstrap.session",
        "Map session &6{session_id} &7using scheme &6{session_scheme} &7created"),
    SELECTED_SESSION("bootstrap.session",
        "Map session &6{session_id} &7selected"),
    DESELECTED_SESSION("bootstrap.session",
        "Map session &6{session_id} &7deselected"),
    VERIFY_SESSION_SUCCESS("bootstrap.session",
        "Verification of map session &6{session_id} &7finished without errors"),
    VERIFY_SESSION_FAIL("bootstrap.session",
        "Verification fails with &6{number} &7error(s):"),
    VERIFY_SESSION_FAIL_ENTRY("bootstrap.session",
        "&8- &6{property} &8-> &c{error}"),
    VERIFY_SESSION_FAIL_SHORTCUT("bootstrap.session",
        "&8- &cand &6{number} &cmore..."),

    PROPERTY_CHANGE_TO("parse.info",
        "Property &6{name} &7set to &6{value}"),
    PROPERTY_INFO_HEADER("parse.info",
        "&8---------[&6Property {name}&8]---------"),
    PROPERTY_INFO_PATH("parse.info",
        "&7Path: &6{path}"),
    PROPERTY_INFO_TYPE("parse.info",
        "&7Type: &6{type}"),
    PROPERTY_INFO_VALUE("parse.info",
        "&7Value: &6{value}"),
    PROPERTY_INFO_VALUE_LIST("parse.info",
        "   &8- &6{value}"),
    PROPERTY_CLEAR("parse.info",
        "Property &6{name} &7has been cleared"),
    PROPERTY_LIST_ADDED("parse.info",
        "Property {type} &6{name} &7added: &6{value}"),
    PROPERTY_LIST_ADDED_ENTRY("parse.info",
        "   &8- &6{value}"),
    PROPERTY_LIST_REMOVED("parse.info",
        "Property {type} &6{name} &7removed &6{value}"),
    PROPERTY_LIST_VALUE_NOT_FOUND("parse.info",
        "Value &6{value} &7not found in property list &6{name}"),

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
        "&6{property} &cis undefined"),
    INVALID_PROPERTY("parse.error",
        "Invalid property {property}"),

    ERROR_MESSAGE("parse.error",
        "&6{property} &8-> &c{text}"),

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
