package team.unnamed.mappa.datasource.database.sql;

public enum SQLType {
    MYSQL(""),
    SQLITE(""),
    H2("org.h2.Driver"),
    POSTGRESQL("");

    // Code from 2020
    // //{hostname}:{port}/{database}
    public static final String JDBC_URL = "jdbc:{prefix}:";

    private final String driver;

    SQLType(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }
}
