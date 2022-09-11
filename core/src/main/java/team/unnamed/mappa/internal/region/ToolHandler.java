package team.unnamed.mappa.internal.region;

import team.unnamed.mappa.internal.tool.Tool;

public interface ToolHandler {
    String VECTOR_TOOL = "vector-tool";
    String CENTERED_VECTOR_TOOL = "centered-vector-tool";
    String PRECISE_VECTOR_TOOL = "precise-vector-tool";
    String MIRROR_VECTOR_TOOL = "mirror-vector-tool";
    String ARMOR_STAND_TOOL = "armor-stand-tool";
    String REGION_RADIUS_TOOL = "region-radius-tool";
    String CUSTOM_REGION_RADIUS_TOOL = "custom-region-radius-tool";
    String YAW_PITCH_TOOL = "yaw-pitch-tool";
    String CENTERED_YAW_PITCH_TOOL = "centered-yaw-pitch-tool";
    String CHUNK_TOOL = "chunk-tool";
    String SCANNER_VECTOR_TOOL = "scanner-vector-tool";

    String REGION_RADIUS = "region-radius";

    String REGION_X_RADIUS = "region-x-radius";
    String REGION_Y_PLUS_RADIUS = "region-y-plus-radius";
    String REGION_Y_MINUS_RADIUS = "region-y-minus-radius";
    String REGION_Z_RADIUS = "region-z-radius";

    String SCAN_PATH = "scan-path";
    String SCAN_SCHEME = "scan-scheme";
    String SCAN_RADIUS = "scan-radius";
    String SCAN_DELETE_BLOCK = "scan-delete-block";
    String SCAN_DELETE_MARKER = "scan-delete-marker";

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
