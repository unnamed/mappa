package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.MappaPlayer;

public interface MappaPlayerEvent extends MappaEvent {

    MappaPlayer getPlayer();
}
