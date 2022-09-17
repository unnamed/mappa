package team.unnamed.mappa.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import team.unnamed.mappa.database.AbstractMapSessionRepository;
import team.unnamed.mappa.model.map.MapSession;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class HikariMapSessionRepository extends AbstractMapSessionRepository {
    private final HikariPool pool;
    private final SQLDialect dialect;

    public HikariMapSessionRepository(HikariConfig config, SQLDialect dialect) {
        this.pool = new HikariPool(config);
        this.dialect = dialect;
    }

    @Override
    public MapSession get(String id) throws SQLException {
        // How to-do unreadable code:
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(dialect.query(id));
             ResultSet resultSet = statement.executeQuery()) {

        }
        return null;
    }

    @Override
    public void put(String id, MapSession object) {

    }

    @Override
    public boolean contains(String id) {
        return false;
    }

    @Override
    public MapSession remove(String id) {
        return null;
    }

    protected MapSession deserialize(ResultSet set) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        while (set.next()) {
            ResultSetMetaData data = set.getMetaData();
            for (int i = 0; i < data.getColumnCount(); i++) {
                String columnName = data.getColumnName(i);
                map.put(columnName, set.getObject(i));
            }
        }
        return super.deserialize(map);
    }
}
