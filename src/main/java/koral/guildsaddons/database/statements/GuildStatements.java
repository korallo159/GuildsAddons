package koral.guildsaddons.database.statements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import koral.guildsaddons.guilds.CustomTabList;
import koral.guildsaddons.guilds.Guild;
import koral.guildsaddons.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static koral.guildsaddons.database.DatabaseConnection.hikari;

public class GuildStatements extends Statements {

    public static void createGuildQuery(Guild guild) {
        stringSetter("INSERT INTO Guilds (NAME, TAG, data) VALUES (?,?,?) ON DUPLICATE KEY UPDATE NAME=?",
                guild.name, guild.tag, GuildStatements.serialize(guild), guild.name);
    }

    public static void updateData(Guild guild) {
        setter("UPDATE Guilds SET data=?, points=? WHERE NAME=?", statement -> {
            statement.setString(1, serialize(guild));
            statement.setDouble(2, guild.points);
            statement.setString(3, guild.name);
        });
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

    public static List<Pair<String, Integer>> getTopGuilds() {
        return getter("SELECT * FROM Guilds ORDER BY points DESC LIMIT " + CustomTabList.rankSlots, statement -> {}, resultSet -> {
            List<Pair<String, Integer>> result = new ArrayList<>();

            while (resultSet.next()) {
                try {
                    result.add(new Pair<>(
                            GuildStatements.deserialize((JSONObject) new JSONParser().parse(resultSet.getString("data"))).name,
                            (int) resultSet.getDouble("points"))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return result;
        });
    }

}
