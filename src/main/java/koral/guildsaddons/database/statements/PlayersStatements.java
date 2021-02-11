package koral.guildsaddons.database.statements;

import koral.guildsaddons.guilds.Guild;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static koral.guildsaddons.database.DatabaseConnection.hikari;

public class PlayersStatements extends Statements {
    private static String standardGetter(String columnLabel, String playerName) {
        return stringGetter(
                "SELECT " + columnLabel + " FROM Players WHERE NICK=?",
                resultSet -> resultSet.next() ? resultSet.getString(columnLabel) : null,
                playerName
        );
    }


    // Sync
    public static void createPlayerQuery(Player player) {
        stringSetter("INSERT INTO Players (NICK, UUID) VALUES (?,?) ON DUPLICATE KEY UPDATE NICK=?",
                player.getName(), player.getUniqueId().toString(), player.getName());
    }

    public static void setPlayerData(Player player, String data) {
        stringSetter("UPDATE Players SET playerdata=? WHERE NICK=?", data, player.getName());
    }

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

    public static String getMysqlPlayerData(Player player) {
        return standardGetter("playerdata", player.getName());
    }

    public static void setHomeData(Player player, String data){
        stringSetter("UPDATE Players SET homes=? WHERE NICK=?", data, player.getName());
    }
    public static String getHomeData(String playerName){
        return standardGetter("homes", playerName);
    }



    public static void setItemShopData(String playername, String data){
        stringSetter("UPDATE Players SET itemshop=? WHERE NICK=?", data, playername);
    }
    public static String getItemShopData(String playerName){
        return standardGetter("itemshop", playerName);
    }

    public static void setKillsData(String playername, int kills){
        setter("UPDATE Players SET kills=? WHERE NICK=?", statement -> {
            statement.setInt(1, kills);
            statement.setString(2, playername);
        });
    }

    public static void setDeathsData(String playername, int deaths){
        setter("UPDATE Players SET deaths=? WHERE NICK=?", statement -> {
            statement.setInt(1, deaths);
            statement.setString(2, playername);
        });
    }

    public static void setPointsData(String playername, double points){
        setter("UPDATE Players SET points=? WHERE NICK=?", statement -> {
            statement.setDouble(1, points);
            statement.setString(2, playername);
        });
    }

    public static void setGuild(String playerName, Guild guild) {
        stringSetter("UPDATE Players SET guild=? WHERE NICK=?", guild == null ? null : guild.name, playerName);
    }
    public static String getGuildName(String playerName) {
        return standardGetter("guild", playerName);
    }
}
