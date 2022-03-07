package team.unnamed.mappa.model.map.property;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.object.Condition;

import java.util.function.Function;

public class MapNodeProperty implements MapProperty {
    @NotNull
    protected final String name;
    @NotNull
    protected final Condition condition;
    @NotNull
    protected final Function<Object, Object> postProcessing;
    protected final boolean optional;
    protected boolean buildProperty;
    protected Object value;

    public static Builder builder(String node) {
        return new Builder(node);
    }

    public static Builder builder(MapNodeProperty property) {
        return builder(property.name)
            .condition(property.condition)
            .postProcessing(property.postProcessing)
            .optional(property.optional)
            .buildProperty(property.buildProperty);
    }

    public MapNodeProperty(@NotNull String name,
                           @NotNull Condition condition,
                           @NotNull Function<Object, Object> postProcessing,
                           boolean optional) {
        this(name, condition, postProcessing, optional, false);
    }

    public MapNodeProperty(@NotNull String name,
                           @NotNull Condition condition,
                           @NotNull Function<Object, Object> postProcessing,
                           boolean optional,
                           boolean buildProperty) {
        this.name = name;
        this.condition = condition;
        this.postProcessing = postProcessing;
        this.optional = optional;
        this.buildProperty = buildProperty;
    }

    @Override
    public void parseValue(Object newValue) {
        String errorMessage = condition.pass(newValue);
        if (errorMessage != null) {
            return;
        }

        this.value = postProcessing.apply(newValue);
    }

    @Override
    public void bypassParseValue(Object newValue) {
        this.value = newValue;
    }

    @Override
    public void clearValue() {
        this.value = null;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public Function<Object, Object> getPostProcessing() {
        return postProcessing;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean isBuildProperty() {
        return buildProperty;
    }

    @Override
    @Nullable
    public Object getValue() {
        return value;
    }

    @Override
    public @NotNull Condition getCondition() {
        return condition;
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static class Builder {
        private final String node;
        private Condition condition;
        private Function<?, ?> postProcessing;
        private boolean optional;
        private boolean buildProperty;

        public Builder(String node) {
            this.node = node;
        }

        public Builder condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Builder conditionOfType(Class<?> type) {
            return condition(Condition.ofType(type));
        }

        public Builder postProcessing(Function<?, ?> postProcessing) {
            this.postProcessing = postProcessing;
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder buildProperty(boolean buildProperty) {
            this.buildProperty = buildProperty;
            return this;
        }

        public MapProperty build() {
            if (postProcessing == null) {
                postProcessing = Function.identity();
            }
            return new MapNodeProperty(node,
                condition,
                (Function<Object, Object>) postProcessing,
                buildProperty,
                optional);
        }
    }
}