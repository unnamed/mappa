package team.unnamed.mappa.internal.event.bus;

import team.unnamed.mappa.internal.event.MappaEvent;

import java.util.function.Consumer;

public abstract class AbstractListenerModule implements ListenerModule {
    private EventBus internalBus;

    @Override
    public <T extends MappaEvent> ListenerModule bind(Class<T> event, Listener.Priority priority, Consumer<T> consumer) {
        return bind(event, priority.ordinal(), consumer);
    }

    @Override
    public <T extends MappaEvent> ListenerModule bind(Class<T> event, int priority, Consumer<T> consumer) {
        internalBus.listen(event, Listener.of(priority, consumer));
        return this;
    }

    @Override
    public <T extends MappaEvent> ListenerModule bind(Class<T> event, Consumer<T> consumer) {
        internalBus.listen(event, Listener.of(0, consumer));
        return this;
    }

    @Override
    public abstract void configure();

    @Override
    public void setEventBus(EventBus eventBus) {
        this.internalBus = eventBus;
    }
}
