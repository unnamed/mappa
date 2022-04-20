package team.unnamed.mappa.object;

public enum TranslationNode implements TextDefault {

    SCHEME_LOADED("bootstrap",
        "&6{number} &7scheme(s) loaded"),
    SCHEME_COMMAND_LOADED("bootstrap",
        "New command &6{name} &7with aliases &6{aliases} &7from scheme &6{scheme_name}"),
    LOAD_SUCCESSFULLY("bootstrap",
        "Mappa bootstrap loaded successfully"),
    SESSIONS_LOADED("bootstrap",
        "&6{number} &7session(s) loaded"),
    SESSIONS_RESUMED("bootstrap",
        "&6{number} &7session(s) resume"),
    NO_SESSIONS_TO_RESUME("bootstrap",
        "There is no session to resume."),

    NEW_SESSION("bootstrap.session",
        "Map session of &6{map_name} &7using scheme &6{map_scheme} &7created"),
    VERIFY_SESSION_SUCCESS("bootstrap.session",
        "Verification of session &6{session_id} &7finished without errors"),
    VERIFY_SESSION_FAIL("bootstrap.session",
        "Verification fails with &6{number} &7error(s):"),
    VERIFY_SESSION_FAIL_ENTRY("bootstrap.session",
        "&8- &c{error}"),
    VERIFY_SESSION_FAIL_SHORTCUT("bootstrap.session",
        "&8- &cand {number} more..."),

    PROPERTY_CHANGE_TO("parse.error",
        "Property &6{name} &7set to &6{value}"),
    PROPERTY_INFO_HEADER("parse.error",
        "&8---------[&6Property {name}&8]---------"),
    PROPERTY_INFO_PATH("parse.error",
        "&7Path: &6{path}"),
    PROPERTY_INFO_TYPE("parse.error",
        "&7Type: &6{type}"),
    PROPERTY_INFO_VALUE("parse.error",
        "&7Value: &6{value}"),
    PROPERTY_INFO_VALUE_LIST("parse.error",
        "   &8- &6{value}"),
    PROPERTY_CLEAR("parse.error",
        "Property &6{name} &7has been cleared"),
    PROPERTY_LIST_ADDED("parse.error",
        "Property list &6{name} &7added &6{value}"),
    PROPERTY_LIST_REMOVED("parse.error",
        "Property list &6{name} &7removed &6{value}"),
    PROPERTY_LIST_VALUE_NOT_FOUND("parse.error",
        "Value &6{value} &7not found in property list &6{name}"),

    PARENT_CONFIG_NOT_FOUND("parse.error",
        "Invalid type of parameter {parameter}, require: {type}"),

    INVALID_TYPE("parse.error",
        "Invalid type of parameter {parameter}, require: {type}"),
    NUMBER_NON_POSITIVE("parse.error",
        "Number {number} is not positive"),
    NUMBER_NON_NEGATIVE("parse.error",
        "Number {number} is not negative"),

    FLAG_CONFLICT("parse.error",
        "Flag key {key} conflicts with {conflict}"),
    FLAG_DUPLICATION("parse.error",
        "Flag key {key} duplication in parse"),
    BUILD_PROPERTY_NOT_NAME("parse.error",
        "Build property {path} does not have a name"),
    CLONE_PATH_NOT_FOUND("parse.error",
        "Trying to clone path {path} found null"),
    UNDEFINED_PROPERTY("parse.error",
        "{property} is undefined"),
    INVALID_PROPERTY("parse.error",
        "Invalid property {property}"),

    ;

    private final String path;
    private final TextDefaultNode textNode;

    TranslationNode(String path, String message) {
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
