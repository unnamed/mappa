package team.unnamed.mappa;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mappa.internal.clipboard.ClipboardHandler;
import team.unnamed.mappa.internal.event.bus.EventBus;
import team.unnamed.mappa.internal.message.MappaTextHandler;
import team.unnamed.mappa.internal.player.PlayerRegistry;
import team.unnamed.mappa.internal.region.RegionRegistry;
import team.unnamed.mappa.internal.region.ToolHandler;
import team.unnamed.mappa.model.visualizer.Visualizer;

import java.io.File;

public interface MappaAPI {

    MappaPlatform getPlatform();

    File getDataFolder();

    @NotNull
    EventBus getEventBus();

    @NotNull
    MappaTextHandler getTextHandler();

    @NotNull
    RegionRegistry getRegionRegistry();

    @NotNull
    PlayerRegistry<? extends Object> getPlayerRegistry();

    @NotNull
    ToolHandler getToolHandler();

    @NotNull
    ClipboardHandler getClipboardHandler();

    @Nullable
    Visualizer getVisualizer();

    boolean initApi();
}
