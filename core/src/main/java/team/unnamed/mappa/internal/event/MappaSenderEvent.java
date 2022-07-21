package team.unnamed.mappa.internal.event;

public abstract class MappaSenderEvent implements MappaEvent {
    protected final Object sender;

    protected MappaSenderEvent(Object sender) {
        this.sender = sender;
    }

    public Object getSender() {
        return sender;
    }
}
