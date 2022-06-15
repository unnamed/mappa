package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.internal.tool.Tool;

public interface ToolHandler {
    String VECTOR_TOOL = "vector-tool";
    String CENTERED_VECTOR_TOOL = "centered-vector-tool";
    String PRECISE_VECTOR_TOOL = "precise-vector-tool";
    String MIRROR_VECTOR_TOOL = "mirror-vector-tool";
    String YAW_PITCH_TOOL = "yaw-pitch-tool";
    String CENTERED_YAW_PITCH_TOOL = "centered-yaw-pitch-tool";
    String CHUNK_TOOL = "chunk-tool";
    String SCANNER_VECTOR_TOOL = "scanner-tool";

    String SCAN_PATH = "scan-path";
    String SCAN_SCHEME = "scan-scheme";
    String SCAN_RADIUS = "scan-radius";

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
