package team.unnamed.mappa;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;

public interface MappaAPI {

    MappaBootstrap getBootstrap();

    @Nullable RegionRegistry getRegionRegistry();

    @Nullable ToolHandler getToolHandler();
}
