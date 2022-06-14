package team.unnamed.mappa;

import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;

public interface MappaAPI {

    MappaBootstrap getBootstrap();

    RegionRegistry getRegionRegistry();

    ToolHandler getToolHandler();
}
