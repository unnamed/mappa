package team.unnamed.mappa.object;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.throwable.DuplicateFlagException;
import team.unnamed.mappa.util.TypeUtils;

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
        return value -> !clazz.isAssignableFrom(value.getClass())
            ? TextNode.withFormal("parse.error.invalid-type",
            "{type}", clazz.getSimpleName())
            : null;
    }

    /**
     * Check an object to complete all the requirements.
     * @param value Object to check.
     * @return Error message if is any.
     */
    TextNode pass(Object value);

    default Condition concat(Condition condition) {
        return value -> {
            TextNode errMessage = pass(value);
            return errMessage == null ? condition.pass(value) : errMessage;
        };
    }

    class Builder<T> {
        private final Class<?> type;
        private final Map<String, Entry<T>> conditions = new LinkedHashMap<>();

        public Builder(Class<T> type) {
            this.type = TypeUtils.primitiveToWrapper(type);
        }

        public Builder<T> filter(String key, Predicate<T> predicate, TextNode node) throws DuplicateFlagException {
            if (conditions.containsKey(key)) {
                throw new DuplicateFlagException("Flag key is blocked or already set: " + key);
            }
            conditions.put(key, new Entry<>(predicate, node));
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
                    return TextNode.withFormal("parse.error.invalid-type",
                        "{type}", type.getSimpleName());
                }

                T t = (T) value;
                for (Map.Entry<String, Entry<T>> mapEntry : conditions.entrySet()) {
                    Entry<T> entry = mapEntry.getValue();
                    if (entry == null) {
                        continue;
                    }
                    Predicate<T> condition = entry.getPredicate();
                    if (!condition.test(t)) {
                        return entry.getTextNode();
                    }
                }
                return null;
            };
        }
    }

    class Entry<T> {
        private final Predicate<T> predicate;
        private final TextNode node;

        public Entry(Predicate<T> predicate, TextNode node) {
            this.predicate = predicate;
            this.node = node;
        }

        public Predicate<T> getPredicate() {
            return predicate;
        }

        public TextNode getTextNode() {
            return node;
        }
    }
}
