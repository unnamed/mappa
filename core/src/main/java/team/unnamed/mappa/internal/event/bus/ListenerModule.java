package team.unnamed.mappa.internal.event.bus;

import team.unnamed.mappa.internal.event.MappaEvent;

import java.util.function.Consumer;

public interface ListenerModule {

    <T extends MappaEvent> ListenerModule bind(Class<T> event, Listener.Priority priority, Consumer<T> consumer);

    <T extends MappaEvent> ListenerModule bind(Class<T> event, int priority, Consumer<T> consumer);
    <T extends MappaEvent> ListenerModule bind(Class<T> event, Consumer<T> consumer);

    void configure();

    void setEventBus(EventBus eventBus);
}
