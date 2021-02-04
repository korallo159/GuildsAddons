package koral.guildsaddons.database.statements;

import koral.guildsaddons.model.Home;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.AccountLockedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static koral.guildsaddons.database.DatabaseConnection.hikari;

public class PlayersStatements {

    // Sync
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
            closeAll(connection, statement);
        }
    }

    // Async
    public static void setPlayerData(Player player, String data){
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            connection = hikari.getConnection();

            statement = connection.prepareStatement("SELECT * FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());
            String update = "UPDATE Players SET playerdata=? WHERE NICK=?";
            statement = connection.prepareStatement(update);

            statement.setString(1, data);
            statement.setString(2, player.getName());
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(connection, statement);
        }
    }

    // Async
    public static void updatePlayerData(Player player, Map<String, Integer> updateMap){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String oldData = null;
        String newData;

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement("SELECT playerdata FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());
            resultSet = statement.executeQuery();

            while(resultSet.next()) {
                oldData = resultSet.getString("playerdata");
            }

            statement = connection.prepareStatement("SELECT * FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());

            String update = "UPDATE Players SET playerdata=? WHERE NICK=?";
            statement = connection.prepareStatement(update);

            if (oldData != null) {
                JSONObject oldJson = (JSONObject) new JSONParser().parse(oldData);
                updateMap.forEach((str, amount) -> oldJson.put(str, (int) (long) oldJson.getOrDefault(str, 0L) + amount));
                newData = oldJson.toJSONString();
            } else
                newData = new JSONObject(updateMap).toJSONString();

            statement.setString(1, newData);
            statement.setString(2, player.getName());
            statement.execute();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        } finally {
            closeAll(connection, statement, resultSet);
        }
    }

    // Async
    public static String getMysqlPlayerData(Player player) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "SELECT playerdata FROM Players WHERE NICK=?";

        String result = null;

        try{
            connection = hikari.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, player.getName());
            resultSet = statement.executeQuery();
            while(resultSet.next()) {
                result = resultSet.getString("playerdata");
            }
        }catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeAll(connection, statement, resultSet);
        }

        return result;
    }

    public static void setHomeData(Player player, String data){
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            connection = hikari.getConnection();

            statement = connection.prepareStatement("SELECT * FROM Players WHERE NICK=?");
            statement.setString(1, player.getName());
            String update = "UPDATE Players SET homes=? WHERE NICK=?";
            statement = connection.prepareStatement(update);

            statement.setString(1, data);
            statement.setString(2, player.getName());
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(connection, statement);
        }
    }

    public static String getHomeData(String playerName){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "SELECT homes FROM Players WHERE NICK=?";

        String result = null;

        try{
            connection = hikari.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, playerName);
            resultSet = statement.executeQuery();
            while(resultSet.next()){
                result = resultSet.getString("homes");
            }
        }catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeAll(connection, statement, resultSet);
        }

        return result;
    }



    private static void closeAll(AutoCloseable... toClose) {
        for (AutoCloseable closeable : toClose)
            if (closeable != null)
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }
}
