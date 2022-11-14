package team.unnamed.mappa.datasource.file;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum FileType {
    YAML("yml"), XML(), JSON();

    @Nullable
    private final String altFormat;

    private static final Map<String, FileType> EXTENSIONS;

    static {
        Map<String, FileType> map = new HashMap<>();
        for (FileType type : values()) {
            String name = type.name().toLowerCase();
            map.put(name, type);
            String formatAlt = type.getAltFormat();
            if (formatAlt != null) {
                map.put(formatAlt, type);
            }
        }

        EXTENSIONS = Collections.unmodifiableMap(map);
    }

    FileType() {
        this(null);
    }

    FileType(@Nullable String alt) {
        this.altFormat = alt;
    }

    public @Nullable String getAltFormat() {
        return altFormat;
    }

    public static FileType byExtension(File file) {
        String name = file.getName();
        int index = name.indexOf(".");
        return index == -1
            ? null
            : EXTENSIONS.get(name.substring(0, index));
    }
}
