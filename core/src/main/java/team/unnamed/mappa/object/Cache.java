package team.unnamed.mappa.object;

import java.util.concurrent.TimeUnit;

public class Cache<T> {
    private final long intervalMillis;

    private T object;
    private long ends;

    public Cache(int intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public Cache(long time, TimeUnit unit) {
        this.intervalMillis = unit.toMillis(time);
    }

    public void set(T object) {
        this.object = object;
        this.ends = System.currentTimeMillis() + intervalMillis;
    }

    public T get() {
        invalidateIfNeeded();
        return object;
    }

    protected void invalidateIfNeeded() {
        if (object != null && ends()) {
            object = null;
        }
    }

    public boolean ends() {
        return System.currentTimeMillis() >= ends;
    }
}
