package team.unnamed.mappa.datasource;

public interface DataSource {

    enum Type {
        DATABASE, FILE, UNKNOWN
    }

    String name();

    Type getType();
}
