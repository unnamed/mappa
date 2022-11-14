package team.unnamed.mappa.bukkit;

import org.bukkit.scheduler.BukkitRunnable;
import team.unnamed.mappa.model.visualizer.PropertyVisual;
import team.unnamed.mappa.model.visualizer.Visual;
import team.unnamed.mappa.model.visualizer.Visualizer;

import java.util.Set;

public class VisualizerTask extends BukkitRunnable {
    private final MappaPlugin plugin;
    private final Visualizer visualizer;

    public VisualizerTask(MappaPlugin plugin) {
        this.plugin = plugin;
        this.visualizer = plugin.getVisualizer();
    }

    public void start(int tickFrequency) {
        runTaskTimer(plugin, tickFrequency, tickFrequency);
    }

    @Override
    public void run() {
        for (Set<PropertyVisual> visuals : visualizer.getEntityVisuals().values()) {
            for (PropertyVisual visual : visuals) {
                visual.render();
            }
        }

        for (Visual visual : visualizer.getSelectionVisuals().values()) {
            visual.render();
        }
    }
}
