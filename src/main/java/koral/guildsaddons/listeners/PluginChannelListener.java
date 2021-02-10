package koral.guildsaddons.listeners;

import koral.guildsaddons.database.statements.GuildStatements;
import koral.guildsaddons.guilds.Guild;
import koral.sectorserver.SectorServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

@SuppressWarnings("unused")
public class PluginChannelListener implements PluginMessageListener {
    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface FromForward {
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("BungeeCord")) {
            String subchannel = "Brak subchannelu";

            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
                subchannel = in.readUTF();

                Method met = PluginChannelListener.class.getDeclaredMethod(subchannel, DataInputStream.class);
                if (met.isAnnotationPresent(FromForward.class))
                    in = fromForward(in);
                met.invoke(this, in);
            } catch (NoSuchMethodException e) {
            } catch (Throwable e) {
                System.out.println("Problem z odbieraniem subchannelu " + subchannel);
                e.printStackTrace();
            }
        }
    }
    private DataInputStream fromForward(DataInputStream in) throws IOException {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        return new DataInputStream(new ByteArrayInputStream(msgbytes));
    }


    @FromForward
    void guildMsg(DataInputStream in) throws IOException {
        String guildName = in.readUTF();
        String msg = in.readUTF();

        SectorServer.doForNonNull(Guild.fromNameUnSafe(guildName), guild -> guild.sendToMembersOnlyThere(msg));
    }
    @FromForward
    void updateGuild(DataInputStream in) throws IOException, ParseException, IllegalAccessException {
        String guildName = in.readUTF();
        String json = in.readUTF();

        Guild guild = Guild.fromNameUnSafe(guildName);
        if (guild == null) return;
        Guild newGuild = GuildStatements.deserialize((JSONObject) new JSONParser().parse(json));
        for (Field field : Guild.class.getDeclaredFields())
            if (!Modifier.isStatic(field.getModifiers()) && !Objects.equals(field.get(guild), field.get(newGuild))) {
                field.set(guild, field.get(newGuild));
                System.out.println(String.format("Ustawiono gildi %s wartość %s na %s", guild.name, field.getName(), field.get(guild)));
            }
    }
    @FromForward
    void deleteGuild(DataInputStream in) throws IOException {
        String guildName = in.readUTF();

        SectorServer.doForNonNull(Guild.fromNameUnSafe(guildName), Guild::deleteUnSafe);
    }
}
