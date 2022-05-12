package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.internal.tool.Tool;

public interface ToolHandler {
    String VECTOR_TOOL = "vector-tool";
    String PRECISE_VECTOR_TOOL = "precise-vector-tool";
    String YAW_PITCH_TOOL = "yaw-pitch-tool";
    String CHUNK_TOOL = "chunk-tool";

    static ToolHandler newToolHandler() {
        return new DefaultToolHandler();
    }

    <T> void registerTool(Tool<T> tool);

    @SuppressWarnings({"rawtypes", "unchecked"})
    default void registerTools(Tool... tools) {
        for (Tool tool : tools) {
            registerTool(tool);
        }
    }

    <T> Tool<T> getToolById(String toolId, T entity);
}
