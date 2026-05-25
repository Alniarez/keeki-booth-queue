package de.alniarez.store;

import org.h2.jdbcx.JdbcConnectionPool;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private final JdbcConnectionPool pool;

    public Database(String path) throws SQLException {
        pool = JdbcConnectionPool.create("jdbc:h2:file:" + path + ";DB_CLOSE_DELAY=-1", "sa", "");
        try (Connection conn = pool.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bookings (
                    date VARCHAR(10) NOT NULL,
                    time VARCHAR(5)  NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    code CHAR(5)     NOT NULL UNIQUE
                )
            """);
        }
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }
}
