package team.unnamed.mappa.internal.command;

import me.fixeddev.commandflow.annotated.CommandClass;
import me.fixeddev.commandflow.annotated.ReflectionInstanceCreator;
import me.fixeddev.commandflow.annotated.SubCommandInstanceCreator;
import team.unnamed.mappa.MappaAPI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Based on {@link ReflectionInstanceCreator}
 */
public class ReflectionMappaInstanceCreator implements SubCommandInstanceCreator {
    private final MappaAPI api;

    public ReflectionMappaInstanceCreator(MappaAPI api) {
        this.api = api;
    }

    @Override
    public CommandClass createInstance(Class<? extends CommandClass> clazz, CommandClass parent) {
        try {
            boolean useUpperClass = true;

            Constructor<?> constructor;
            try {
                constructor = clazz.getConstructor(MappaAPI.class);
            } catch (NoSuchMethodException e) {
                constructor = clazz.getConstructor();
                useUpperClass = false;
            }

            boolean accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            CommandClass instance = useUpperClass
                ? (CommandClass) constructor.newInstance(api)
                : (CommandClass) constructor.newInstance();

            constructor.setAccessible(accessible);
            return instance;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
