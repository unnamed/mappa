package team.unnamed.mappa.internal.event.bus;

import team.unnamed.mappa.internal.event.MappaNewSessionEvent;
import team.unnamed.mappa.internal.event.MappaPropertySetEvent;
import team.unnamed.mappa.internal.event.MappaSavedEvent;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.ParseException;

public class PlatformListenerModule extends AbstractListenerModule {

    @Override
    public void configure() {
        bind(MappaNewSessionEvent.class,
            event -> {
                try {
                    event.getPlayer().showSetup();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });
        bind(MappaSavedEvent.class,
            event -> {
                MappaPlayer player = event.getPlayer();
                MapSession otherSession = player.getMapSession();
                String sessionId = event.getMapSessionId();
                if (otherSession == null || !sessionId.equals(otherSession.getId())) {
                    return;
                }

                player.send(
                    TranslationNode
                        .DESELECTED_SESSION
                        .formalText(),
                    otherSession);
            });
        bind(MappaPropertySetEvent.class,
            event -> {

            });
    }
}
