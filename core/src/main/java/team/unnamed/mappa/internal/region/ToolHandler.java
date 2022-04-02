package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.internal.tool.Tool;

public interface ToolHandler {
    String VECTOR_TOOL = "vector-tool";
    String CHUNK_TOOL = "chunk-tool";

    static ToolHandler newToolHandler() {
        return new DefaultToolHandler();
    }

    <T> void registerTool(Tool<T> tool);

    <T> Tool<T> getToolById(String toolId, T entity);
}
