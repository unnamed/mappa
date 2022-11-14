package team.unnamed.mappa.internal.event;

import team.unnamed.mappa.model.MappaPlayer;
import team.unnamed.mappa.model.region.RegionSelection;

public class MappaRegionSelectEvent extends MappaSenderEvent {
    private final RegionSelection<?> selection;

    public MappaRegionSelectEvent(MappaPlayer sender, RegionSelection<?> selection) {
        super(sender);
        this.selection = selection;
    }

    public RegionSelection<?> getSelection() {
        return selection;
    }

    public Class<?> getType() {
        return selection.getType();
    }
}
