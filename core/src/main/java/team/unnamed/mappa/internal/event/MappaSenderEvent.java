package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.MappaPlayer;

public abstract class MappaSenderEvent implements MappaPlayerEvent {
    protected final MappaPlayer sender;

    protected MappaSenderEvent(MappaPlayer sender) {
        this.sender = sender;
    }

    @Override
    public MappaPlayer getPlayer() {
        return sender;
    }

    public Object getEntitySender() {
        return sender.asEntity();
    }
}
