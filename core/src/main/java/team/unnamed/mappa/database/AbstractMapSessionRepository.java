package team.unnamed.mappa.database;

import team.unnamed.mappa.model.map.MapSession;

import java.util.Map;

public abstract class AbstractMapSessionRepository implements MapSessionRepository {

    protected MapSession deserialize(Map<String, Object> map) {
        map.get("");
        return null;
    }
}
