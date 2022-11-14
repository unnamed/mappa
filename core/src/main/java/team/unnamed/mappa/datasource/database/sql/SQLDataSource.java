package team.unnamed.mappa.datasource.database.sql;

import team.unnamed.mappa.datasource.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLDataSource extends DataSource {

    static SQLDataSource of(String typeName) {
        SQLType sqlType = SQLType.valueOf(typeName.toUpperCase());
        return null;
    }

    Connection connection() throws SQLException;

    void close();

    SQLType getSQLType();
}
