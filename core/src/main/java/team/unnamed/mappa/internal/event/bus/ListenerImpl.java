package team.unnamed.mappa.internal.event.bus;

import team.unnamed.mappa.internal.event.MappaEvent;

import java.util.function.Consumer;

public class ListenerImpl<T extends MappaEvent> implements Listener<T> {
    private final int priority;
    private final Consumer<T> action;

    ListenerImpl(int priority, Consumer<T> action) {
        this.priority = priority;
        this.action = action;
    }

    @Override
    public Consumer<T> action() {
        return action;
    }

    @Override
    public void call(T event) {
        action.accept(event);
    }

    @Override
    public int priority() {
        return priority;
    }
}
