package koral.guildsaddons.model;

public class Home  {
    private String nick;
    private String homename;
    private String world;
    private String server;
    private double x;
    private double y;
    private double z;

    public Home(String nick, String homename, String world, String server, double x, double y, double z) {
        this.nick = nick;
        this.homename = homename;
        this.world = world;
        this.server = server;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getHomename() {
        return homename;
    }

    public void setHomename(String homename) {
        this.homename = homename;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
