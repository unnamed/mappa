package team.unnamed.mappa.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import team.unnamed.mappa.bukkit.internal.BukkitVisualizer;
import team.unnamed.mappa.model.visualizer.PropertyVisual;
import team.unnamed.mappa.model.visualizer.Visual;

import java.util.List;

public class VisualizerTask extends BukkitRunnable {
    private final MappaPlugin plugin;
    private final BukkitVisualizer visualizer;

    public VisualizerTask(MappaPlugin plugin) {
        this.plugin = plugin;
        this.visualizer = plugin.getVisualizer();
    }

    public void start(int tickFrequency) {
        runTaskTimer(plugin, tickFrequency, tickFrequency);
    }

    @Override
    public void run() {
        for (List<PropertyVisual<Player>> visuals : visualizer.getEntityVisuals().values()) {
            for (PropertyVisual<Player> visual : visuals) {
                visual.render();
            }
        }

        for (Visual visual : visualizer.getSelectionVisual().values()) {
            visual.render();
        }
    }
}
