package team.unnamed.mappa.datasource.source;

import team.unnamed.mappa.datasource.file.FileType;

import java.io.File;

public class FileTargetSourceImpl implements FileTargetSource {
    private final File target;
    private final FileType fileType;
    private boolean override;

    public FileTargetSourceImpl(File target, FileType fileType) {
        this.target = target;
        this.fileType = fileType;
    }

    @Override
    public boolean override() {
        return override;
    }

    @Override
    public void setOverride(boolean override) {
        this.override = override;
    }

    @Override
    public File target() {
        return target;
    }

    @Override
    public FileType getFileType() {
        return fileType;
    }
}
