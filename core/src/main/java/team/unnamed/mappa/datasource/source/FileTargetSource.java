package team.unnamed.mappa.datasource.source;

import team.unnamed.mappa.datasource.DataSource;
import team.unnamed.mappa.datasource.file.FileType;

import java.io.File;
import java.nio.file.Path;

public interface FileTargetSource extends TargetSource{

    static FileTargetSource of(File folder, String file) {
        return of(new File(folder, file));
    }

    static FileTargetSource of(String path) {
        return of(new File(path));
    }

    static FileTargetSource of(Path path) {
        return of(path.toFile());
    }

    static FileTargetSource of(File file) {
        FileType type = FileType.byExtension(file);
        if (type == null) {
            throw new IllegalArgumentException("Format of file " + file.getName() + " is unsupported");
        }

        return new FileTargetSourceImpl(file, type);
    }

    boolean override();

    void setOverride(boolean override);

    File target();

    @Override
    default DataSource.Type getSourceType() {
        return DataSource.Type.FILE;
    }

    FileType getFileType();
}
