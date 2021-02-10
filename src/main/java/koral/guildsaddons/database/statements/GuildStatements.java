package koral.guildsaddons.database.statements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import koral.guildsaddons.guilds.Guild;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static koral.guildsaddons.database.DatabaseConnection.hikari;

public class GuildStatements extends Statements {

    public static void createGuildQuery(Guild guild) {
        stringSetter("INSERT INTO Guilds (NAME, TAG, data) VALUES (?,?,?) ON DUPLICATE KEY UPDATE NAME=?",
                guild.name, guild.tag, GuildStatements.serialize(guild), guild.name);
    }

    public static void updatadeData(Guild guild) {
        stringSetter("UPDATE Guilds SET data=? WHERE NAME=?", serialize(guild), guild.name);
    }

    public static void delete(Guild guild) {
        stringSetter("DELETE FROM Guilds WHERE NAME=?", guild.name);
    }

    public static Guild fromName(String name) {
        return getFrom(name, "NAME");
    }
    public static Guild fromTag(String tag) {
        return getFrom(tag, "TAG");
    }
    public static Guild getFrom(String key, String primaryKey) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT data FROM Guilds WHERE " + primaryKey + "=?";

        String result = null;

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, key);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result = resultSet.getString("data");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(connection, statement, resultSet);
        }

        if (result == null)
            return null;

        try {
            return deserialize((JSONObject) new JSONParser().parse(result));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String serialize(Guild guild) {
        return new GsonBuilder().create().toJson(guild);
    }
    public static Guild deserialize(JSONObject json) {
        return new Gson().fromJson(json.toJSONString(), Guild.class);
    }
    public static Guild deserialize(JsonElement json) {
        return new Gson().fromJson(json.getAsString(), Guild.class);
    }
}
