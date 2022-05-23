package team.unnamed.mappa.throwable;

import me.fixeddev.commandflow.exception.ArgumentParseException;
import team.unnamed.mappa.object.Text;

public class ArgumentTextParseException extends ArgumentParseException {
    private final Text text;

    public ArgumentTextParseException(Text text) {
        super(text.getNode());
        this.text = text;
    }

    public ArgumentTextParseException(Text text, Throwable cause) {
        super(text.getNode(), cause);
        this.text = text;
    }

    public Text getText() {
        return text;
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }
}
