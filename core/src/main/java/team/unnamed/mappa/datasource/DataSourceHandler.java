package team.unnamed.mappa.datasource;

import team.unnamed.mappa.datasource.source.TargetSource;
import team.unnamed.mappa.model.map.MapSession;

import java.util.Collection;

public interface DataSourceHandler {

    MapSession query(String key, TargetSource targetSource);

    void save(MapSession session, TargetSource targetSource);

    default void delete(MapSession session, TargetSource targetSource) {
        delete(session.getId(), targetSource);
    }

    void delete(String key, TargetSource targetSource);
    
    Collection<DataSource> sources();
}
