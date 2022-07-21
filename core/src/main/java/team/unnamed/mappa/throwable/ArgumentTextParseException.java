package team.unnamed.mappa.throwable;

import me.fixeddev.commandflow.exception.ArgumentParseException;
import team.unnamed.mappa.object.Text;

public class ArgumentTextParseException extends ArgumentParseException {
    private final Text text;
    private final Object entities;

    public ArgumentTextParseException(Text text, Object entities) {
        super(text.getNode());
        this.text = text;
        this.entities = entities;
    }

    public ArgumentTextParseException(Text text) {
        super(text.getNode());
        this.text = text;
        this.entities = new Object[0];
    }

    public ArgumentTextParseException(Text text, Throwable cause) {
        super(text.getNode(), cause);
        this.text = text;
        this.entities = new Object[0];
    }

    public Object getEntities() {
        return entities;
    }

    public Text getText() {
        return text;
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }
}
