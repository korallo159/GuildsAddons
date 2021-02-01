package koral.guildsaddons.database.statements;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static koral.guildsaddons.database.DatabaseConnection.hikari;

public class PlayersStatements {

    public static void createPlayerQuery(Player player) {
        Connection connection = null;
        PreparedStatement statement = null;
        String update = "INSERT INTO Players (NICK, UUID) VALUES (?,?) ON DUPLICATE KEY UPDATE NICK=?";


        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());
            statement = connection.prepareStatement(update);
            statement.setString(1, player.getName());
            statement.setString(2, player.getUniqueId().toString());
            statement.setString(3, player.getName());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void updatePlayerData(Player player){
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            connection = hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());
            String update = "UPDATE Players SET playerdata=? WHERE NICK=?";
            statement = connection.prepareStatement(update);

            statement.setString(1, "string");
            statement.setString(2, player.getName());
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void getMysqlPlayerData(Player player){
        Connection connection = null;
        PreparedStatement statement = null;
        String sql = "SELECT playerdata FROM Players WHERE NICK=?";
        try{
            connection = hikari.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, player.getName());
            ResultSet resultSet = statement.executeQuery();
       //     while(resultSet.next()){
                resultSet.next();
                String data = resultSet.getString("playerdata");
                if(data != null) {

                }
     //       }
        }catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if(statement != null){
                try {
                    statement.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

}
