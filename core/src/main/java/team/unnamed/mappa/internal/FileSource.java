package team.unnamed.mappa.internal;

import team.unnamed.mappa.model.map.scheme.MapScheme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public interface FileSource {
    String SCHEME_FORMAT = "scheme-%s.%s";

    FileSource BASIC = (session, folder, formatFile) ->
        new File(folder,
            String.format(SCHEME_FORMAT,
                session.getName(),
                formatFile));

    File file(MapScheme scheme, File folder, String formatFile);

    default FileWriter fileWriter(MapScheme scheme, File folder, String formatFile) throws IOException {
        return new FileWriter(file(scheme, folder, formatFile));
    }
}
