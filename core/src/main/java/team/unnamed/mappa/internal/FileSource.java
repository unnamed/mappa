package team.unnamed.mappa.internal;

import team.unnamed.mappa.model.map.scheme.MapScheme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public interface FileSource {
    String MAP_FORMAT = "/maps/map-%s.%s";

    FileSource SCHEME = withFormat(MAP_FORMAT);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static FileSource withFormat(String format) {
        return (session, folder, extension) -> {
            File file = new File(folder,
                String.format(format,
                    session.getName(),
                    extension));
            File parentFile = file.getParentFile();
            parentFile.mkdirs();
            return file;
        };
    }

    File file(MapScheme scheme, File folder, String extension);

    default FileWriter fileWriter(MapScheme scheme, File folder, String extension) throws IOException {
        return new FileWriter(file(scheme, folder, extension));
    }
}
