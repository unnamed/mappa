package team.unnamed.mappa.object;

public enum TranslationNode implements TextDefault {

    SCHEME_LOADED("bootstrap",
        "{number} scheme(s) loaded"),
    SCHEME_COMMAND_LOADED("bootstrap",
        "New command {name} with aliases {aliases} from scheme {scheme_name}"),
    LOAD_SUCCESSFULLY("bootstrap",
        "Mappa bootstrap loaded successfully"),
    SESSIONS_LOADED("bootstrap",
        "{number} session(s) loaded"),
    SESSIONS_RESUMED("bootstrap",
        "{number} session(s) resume"),

    NEW_SESSION("bootstrap.session",
        "Map session of {map_name} using scheme {map_scheme} created"),
    VERIFY_SESSION_FAIL("bootstrap.session",
        "Verification of session {session_id} fails with {number} error(s)."),

    PROPERTY_CHANGE_TO("parse.error",
        "Property {name} set to {value}"),

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
