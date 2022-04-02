package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.internal.tool.Tool;

import java.util.HashMap;
import java.util.Map;

public class DefaultToolHandler implements ToolHandler {
    protected final Map<Class<?>, Map<String, Tool<?>>> entityToolMap = new HashMap<>();

    @Override
    public <T> void registerTool(Tool<T> tool) {
        entityToolMap.compute(tool.getEntityType(), (type, tools) -> {
            if (tools == null) {
                tools = new HashMap<>();
            }
            tools.put(tool.getId(), tool);
            return tools;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Tool<T> getToolById(String toolId, T entity) {
        Class<?> entityClass = entity.getClass();
        for (Class<?> clazz : entityToolMap.keySet()) {
            if (clazz == entityClass || clazz.isAssignableFrom(entityClass)) {
                Map<String, Tool<?>> toolMap = entityToolMap.get(clazz);
                return toolMap == null || toolMap.isEmpty()
                    ? null
                    : (Tool<T>) toolMap.get(toolId);
            }
        }
        return null;
    }
}
