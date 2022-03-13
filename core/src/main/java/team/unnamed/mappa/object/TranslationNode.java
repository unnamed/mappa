package team.unnamed.mappa.object;

public enum TranslationNode {

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
        "Trying to clone path {path} found null")

    ;

    private final String node;
    private final String message;

    TranslationNode(String node, String message) {
        this.node = node;
        this.message = message;
    }

    public TextNode text() {
        return TextNode.with(getPath());
    }

    public TextNode with(Object... objects) {
        return TextNode.with(getPath(), objects);
    }

    public TextNode withFormal(Object... objects) {
        return TextNode.withFormal(node, objects);
    }

    public String getName() {
        return name().toLowerCase();
    }

    public String getPath() {
        return node + "." + getName();
    }

    public String getNode() {
        return node;
    }

    public String getMessage() {
        return message;
    }
}
