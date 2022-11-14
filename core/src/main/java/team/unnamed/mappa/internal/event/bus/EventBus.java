package team.unnamed.mappa.internal.event.bus;

import team.unnamed.mappa.internal.event.MappaEvent;

import java.util.*;
import java.util.function.Consumer;

/**
 * Pipeline to listen any mappa event.
 */
public class EventBus {
    private final Map<Class<? extends MappaEvent>, List<Listener<? extends MappaEvent>>> listeners = new LinkedHashMap<>();

    public void install(ListenerModule module) {
        module.setEventBus(this);
        module.configure();
        module.setEventBus(null);
    }

    public <T extends MappaEvent> void listen(Class<T> eventClass, Consumer<T> listener) {
        listen(eventClass, Listener.of(0, listener));
    }

    public <T extends MappaEvent> void listen(Class<T> eventClass, Listener<T> listener) {
        List<Listener<? extends MappaEvent>> consumers = listeners.computeIfAbsent(
            eventClass, list -> new ArrayList<>());
        consumers.add(listener);
        Collections.sort(consumers);
    }

    public List<Listener<? extends MappaEvent>> getListenersOf(Class<? extends MappaEvent> clazz) {
        return listeners.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends MappaEvent> void callEvent(T event) {
        for (Listener<? extends MappaEvent> listener : getListenersOf(event.getClass())) {
            Listener<T> eventListener = (Listener<T>) listener;
            eventListener.call(event);
        }
    }

    public void clearAll() {
        this.listeners.clear();
    }
}
