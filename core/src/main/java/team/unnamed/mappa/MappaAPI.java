package team.unnamed.mappa;

import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.visualizer.Visualizer;

public interface MappaAPI {

    MappaBootstrap getBootstrap();

    RegionRegistry getRegionRegistry();

    ToolHandler getToolHandler();

    @SuppressWarnings("TypeParameterExplicitlyExtendsObject") // To change generic type by implementation
    @Nullable
    Visualizer<? extends Object> getVisualizer();
}
