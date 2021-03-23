package com.github.pwinckles.db.connection.test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Map;

public class Main {

    private static final Map<String, String> DB_DRIVER_MAP = Map.of(
            "postgresql", "org.postgresql.Driver",
            "mariadb", "org.mariadb.jdbc.Driver",
            "mysql", "com.mysql.cj.jdbc.Driver"
    );

    public static void main(String[] args) throws PropertyVetoException {
        if (args.length != 3) {
            System.err.println("Expected 3 arguments: <jdbc url> <username> <password>");
            System.exit(1);
        }

        var url = args[0];
        var username = args[1];
        var password = args[2];

        System.out.printf("# Attempting to connect to DB %s as user %s%n", url, username);

        var parts = url.split(":");
        if (parts.length < 2) {
            System.err.println("# Invalid JDBC url: " + url);
            System.exit(1);
        }
        var dbType = parts[1].toLowerCase();

        var driver = DB_DRIVER_MAP.get(dbType);
        if (driver == null) {
            System.err.println("# Unsupported DB platform: " + dbType);
        }

        var dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(driver);
        dataSource.setJdbcUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setCheckoutTimeout(10000);
        dataSource.setMaxPoolSize(15);
        dataSource.setIdleConnectionTestPeriod(300);
        dataSource.setTestConnectionOnCheckout(true);

        try (var conn = dataSource.getConnection();
             var statement = conn.prepareStatement("SELECT 1")) {
            statement.execute();
            System.out.println("# Successfully connected to DB");
        } catch (SQLException e) {
            System.err.println("# Failed to connect to DB: " + e.getMessage());
            System.exit(1);
        }
    }

}
