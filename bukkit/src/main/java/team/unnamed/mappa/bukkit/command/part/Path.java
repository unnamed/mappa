package team.unnamed.mappa.bukkit.command.part;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Path {
    boolean findAll() default true;

    boolean collect() default false;
}
