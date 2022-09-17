package team.unnamed.mappa.database.sql;

import team.unnamed.mappa.model.map.MapSession;

public interface SQLDialect {

    String[] createTables();

    String update(MapSession session);

    String query(String id);

    String exists(String id);
}
