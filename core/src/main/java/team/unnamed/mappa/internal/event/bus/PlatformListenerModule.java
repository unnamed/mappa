package team.unnamed.mappa.internal.event.bus;

import team.unnamed.mappa.internal.event.MappaNewSessionEvent;
import team.unnamed.mappa.internal.event.MappaPropertySetEvent;
import team.unnamed.mappa.internal.event.MappaSavedEvent;
import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.throwable.ParseException;

public class PlatformListenerModule extends AbstractListenerModule {

    @Override
    public void configure() {
        bind(MappaNewSessionEvent.class,
            event -> {
                MapSession mapSession = event.getMapSession();
                MappaPlayer player = event.getPlayer();
                if (event.getReason() == MappaNewSessionEvent.Reason.RESUMED) {
                    return;
                }

                player.selectMapSession(mapSession);
                try {
                    player.showSetup();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });
        bind(MappaSavedEvent.class,
            event -> {
                MappaPlayer player = event.getPlayer();
                MapSession mapSession = event.getMapSession();
                if (!mapSession.equals(player.getMapSession())) {
                    return;
                }

                player.deselectMapSession();
            });
        bind(MappaPropertySetEvent.class,
            event -> {
                MappaPlayer player = event.getPlayer();
                MapSession mapSession = player.getMapSession();
                if (!mapSession.setup()) {
                    return;
                }

                try {
                    player.showSetup();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}
