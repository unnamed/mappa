package team.unnamed.mappa.datasource.source;

import team.unnamed.mappa.datasource.DataSource;
import team.unnamed.mappa.datasource.database.DatabaseType;

public interface DatabaseTargetSource extends TargetSource {

    static DatabaseTargetSource of() {
        return null;
    }

    @Override
    default DataSource.Type getSourceType() {
        return DataSource.Type.DATABASE;
    }

    DatabaseType getDatabaseType();
}
