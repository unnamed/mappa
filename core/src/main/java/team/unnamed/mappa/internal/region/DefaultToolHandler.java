package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.internal.tool.Tool;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DefaultToolHandler implements ToolHandler {
    protected final Map<String, Tool> tools = new HashMap<>();
    protected final Map<Class<?>, Set<Tool>> toolsByType = new HashMap<>();

    @Override
    public void registerTool(Tool tool) {
        tools.put(tool.getId(), tool);
        Set<Tool> toolSet = toolsByType.computeIfAbsent(
            tool.getSelectionType(), type -> new LinkedHashSet<>());
        toolSet.add(tool);
    }

    @Override
    public Tool getById(String toolId) {
        return tools.get(toolId);
    }

    @Override
    public Set<Tool> getByType(Class<?> clazz) {
        return toolsByType.get(clazz);
    }

    @Override
    public Map<String, Tool> getTools() {
        return tools;
    }
}
