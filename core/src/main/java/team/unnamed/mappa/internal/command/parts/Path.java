package team.unnamed.mappa.internal.command.parts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Path {
    PropertyType find() default PropertyType.PROPERTY;

    boolean collect() default false;
}
