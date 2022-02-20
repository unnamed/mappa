package team.unnamed.mappa.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.mappa.MappaBootstrap;

public class MappaPlugin extends JavaPlugin {
    private final MappaBootstrap bootstrap;

    public MappaPlugin(MappaBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }



}
