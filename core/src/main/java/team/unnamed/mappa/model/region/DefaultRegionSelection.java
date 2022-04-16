package team.unnamed.mappa.model.region;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DefaultRegionSelection<T> implements RegionSelection<T> {
    @NotNull
    protected final Class<T> type;

    protected T first;
    protected T second;

    public DefaultRegionSelection(@NotNull Class<T> type) {
        this.type = type;
    }

    @Override
    public void setFirstPoint(T vector) {
        this.first = vector;
    }

    @Override
    public void setSecondPoint(T vector) {
        this.second = vector;
    }

    @Override
    @NotNull
    public Class<T> getType() {
        return type;
    }

    @Override
    public T getFirstPoint() {
        return first;
    }

    @Override
    public T getSecondPoint() {
        return second;
    }

    @Override
    public String toString() {
        return "DefaultRegionSelection{" +
            "type=" + type +
            ", first=" + first +
            ", second=" + second +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultRegionSelection<?> that = (DefaultRegionSelection<?>) o;
        return type.equals(that.type) && first.equals(that.first) && second.equals(that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
