package koral.guildsaddons.database.statements;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static koral.guildsaddons.database.DatabaseConnection.hikari;

public class Table {

    public static void createTables() {
        try (Connection connection = hikari.getConnection()) {
            for (String create : new String[]{
                    "CREATE TABLE IF NOT EXISTS Players(" +
                            "NICK VARCHAR(16), " +
                            "UUID VARCHAR(36), " +
                            "playerdata TINYTEXT, homes TEXT DEFAULT '[]', " +
                            "itemshop TEXT DEFAULT '', " +
                            "guild VARCHAR(32), " + //TODO: relacja
                            "kills INT DEFAULT 0, " +
                            "deaths INT DEFAULT 0, " +
                            "points DOUBLE DEFAULT 1000.0, " +
                            "PRIMARY KEY (NICK))",
                    "CREATE TABLE IF NOT EXISTS Guilds(" +
                            "NAME VARCHAR(32), " +
                            "TAG VARCHAR(4), " +
                            "data TEXT, " +
                            "points DOUBLE DEFAULT 0.0," +
                            "PRIMARY KEY (NAME))"
            })
                try (PreparedStatement statement = connection.prepareStatement(create)) {
                    statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}