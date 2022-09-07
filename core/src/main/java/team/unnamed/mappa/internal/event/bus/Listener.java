package team.unnamed.mappa.internal.event.bus;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.internal.event.MappaEvent;

import java.util.function.Consumer;

public interface Listener<T extends MappaEvent> extends Comparable<Listener<?>> {

    static <T extends MappaEvent> Listener<T> of(Priority priority,
                                                 Consumer<T> consumer) {
        return of(priority.ordinal(), consumer);
    }

    static <T extends MappaEvent> Listener<T> of(int priority,
                                                 Consumer<T> consumer) {
        return new ListenerImpl<>(priority, consumer);
    }

    Consumer<T> action();

    void call(T event);

    int priority();

    @Override
    default int compareTo(@NotNull Listener<?> o) {
        return Integer.compare(priority(), o.priority());
    }

    enum Priority {
        HIGHEST,
        HIGH,
        NORMAL,
        LOW,
        LOWEST,
        MONITOR
    }
}
