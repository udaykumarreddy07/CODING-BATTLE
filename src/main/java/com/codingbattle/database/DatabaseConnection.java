package com.codingbattle.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resilient Database Connection Manager.
 * Demonstrates Singleton Pattern and Auto-Failover to Embedded H2 if MySQL is unavailable.
 * Guarantees zero failures during college presentations and viva!
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;

    private String activeType = "UNKNOWN";
    private String activeUrl;
    private String activeUser;
    private String activePassword;

    private DatabaseConnection() {
        initConnectionConfig();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void initConnectionConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load db.properties, using fallback defaults", e);
        }

        String mode = props.getProperty("db.mode", "auto");
        String mysqlDriver = props.getProperty("mysql.driver", "com.mysql.cj.jdbc.Driver");
        String mysqlUrl = props.getProperty("mysql.url", "jdbc:mysql://localhost:3306/coding_battle_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        String mysqlUser = props.getProperty("mysql.user", "root");
        String mysqlPass = props.getProperty("mysql.password", "root");

        String h2Driver = props.getProperty("h2.driver", "org.h2.Driver");
        String h2Url = props.getProperty("h2.url", "jdbc:h2:./data/coding_battle_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE");
        String h2User = props.getProperty("h2.user", "sa");
        String h2Pass = props.getProperty("h2.password", "");

        if ("mysql".equalsIgnoreCase(mode) || "auto".equalsIgnoreCase(mode)) {
            try {
                Class.forName(mysqlDriver);
                // Test MySQL connection with 2 second timeout
                DriverManager.setLoginTimeout(2);
                try (Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass)) {
                    if (conn != null && !conn.isClosed()) {
                        activeType = "MySQL";
                        activeUrl = mysqlUrl;
                        activeUser = mysqlUser;
                        activePassword = mysqlPass;
                        LOGGER.info("Successfully connected to MySQL database: " + mysqlUrl);
                        return;
                    }
                }
            } catch (Throwable t) {
                LOGGER.warning("MySQL connection could not be established (" + t.getMessage() + "). Falling back to resilient Embedded H2 database.");
            }
        }

        // Fallback to embedded H2
        try {
            Class.forName(h2Driver);
            activeType = "Embedded H2";
            activeUrl = h2Url;
            activeUser = h2User;
            activePassword = h2Pass;
            LOGGER.info("Initialized resilient Embedded H2 database at: " + h2Url);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load H2 database driver", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (activeUrl == null) {
            initConnectionConfig();
        }
        return DriverManager.getConnection(activeUrl, activeUser, activePassword);
    }

    public String getActiveDatabaseType() {
        return activeType;
    }

    public String getActiveUrl() {
        return activeUrl;
    }
}
