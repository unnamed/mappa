package team.unnamed.mappa.internal.command.parts;

import me.fixeddev.commandflow.annotated.part.PartFactory;
import me.fixeddev.commandflow.part.CommandPart;
import team.unnamed.mappa.MappaAPI;

import java.lang.annotation.Annotation;
import java.util.List;

public class MapSessionFactoryPart implements PartFactory {
    private final MappaAPI api;

    public MapSessionFactoryPart(MappaAPI api) {
        this.api = api;
    }

    @Override
    public CommandPart createPart(String name, List<? extends Annotation> modifiers) {
        boolean sender = getAnnotation(modifiers, Sender.class) != null;
        return new MapSessionSenderPart(name, sender, api.getPlatform());
    }
}
