package team.unnamed.mappa.object;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.throwable.DuplicateFlagException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public interface Condition {
    Condition EMPTY = value -> null;

    static Condition empty() {
        return EMPTY;
    }

    static <T> Condition.Builder<T> builder(Class<T> type) {
        return new Condition.Builder<>(type);
    }

    static Condition ofType(Class<?> clazz) {
        return value -> !clazz.isAssignableFrom(value.getClass()) ? "parse.error.invalid-type." + clazz.getSimpleName().toLowerCase() : null;
    }

    String pass(Object value);

    default Condition concat(Condition condition) {
        return value -> {
            String errMessage = pass(value);
            return errMessage == null ? condition.pass(value) : errMessage;
        };
    }

    class Builder<T> {
        private final Class<T> type;
        private final Map<String, Entry<T>> conditions = new LinkedHashMap<>();

        public Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> filter(String key, Predicate<T> predicate, String node) throws DuplicateFlagException {
            if (conditions.containsKey(key)) {
                throw new DuplicateFlagException("Flag key is blocked or already set: " + key);
            }
            conditions.put(node, new Entry<>(predicate, node));
            return this;
        }

        public Builder<T> block(@NotNull String key) {
            conditions.put(key, null);
            return this;
        }

        @SuppressWarnings("unchecked")
        public Condition build() {
            return value -> {
                if (!type.isAssignableFrom(value.getClass())) {
                    return "parse.error.invalid-type." + type.getSimpleName().toLowerCase();
                }

                T t = (T) value;
                for (Map.Entry<String, Entry<T>> mapEntry : conditions.entrySet()) {
                    Entry<T> entry = mapEntry.getValue();
                    if (entry == null) {
                        continue;
                    }
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
