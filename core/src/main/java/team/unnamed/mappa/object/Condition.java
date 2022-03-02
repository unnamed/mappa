package team.unnamed.mappa.object;

import team.unnamed.mappa.throwable.DuplicateFlagException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public interface Condition {

    static <T> Condition.Builder<T> builder(Class<T> type) {
        return new Condition.Builder<>(type);
    }

    String pass(Object value);

    class Builder<T> {
        private final Class<T> type;
        private final Map<String, Entry<T>> conditions = new LinkedHashMap<>();

        public Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> filter(String key, Predicate<T> predicate, String node) throws DuplicateFlagException {
            if (conditions.putIfAbsent(node, new Entry<>(predicate, node)) != null) {
                throw new DuplicateFlagException("Flag key already exists: "  + key);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        public Condition build() {
            return value -> {
                if (!type.isAssignableFrom(value.getClass())) {
                    return "parse.error.invalid-type";
                }

                T t = (T) value;
                for (Map.Entry<String, Entry<T>> mapEntry : conditions.entrySet()) {
                    Entry<T> entry = mapEntry.getValue();
                    Predicate<T> condition = entry.getPredicate();
                    if (!condition.test(t)) {
                        return entry.getNode();
                    }
                }
                return null;
            };
        }
    }

    class Entry<T> {
        private final Predicate<T> predicate;
        private final String node;

        public Entry(Predicate<T> predicate, String node) {
            this.predicate = predicate;
            this.node = node;
        }

        public Predicate<T> getPredicate() {
            return predicate;
        }

        public String getNode() {
            return node;
        }
    }
}