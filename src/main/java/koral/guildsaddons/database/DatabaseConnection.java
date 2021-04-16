package koral.guildsaddons.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import koral.guildsaddons.GuildsAddons;

public class DatabaseConnection {

    public static HikariDataSource hikari;
//SET session wait_timeout=1200; poprzednio 28800
    public static void configureDbConnection() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(GuildsAddons.getPlugin().getConfig().getString("jdbcurl"));
        hikariConfig.setMaxLifetime(900000); // zeby uniknac wiekszy lifetime hikari niz mysql
        hikariConfig.addDataSourceProperty("user", GuildsAddons.getPlugin().getConfig().getString("username"));
        hikariConfig.addDataSourceProperty("password", GuildsAddons.getPlugin().getConfig().getString("password"));
        hikariConfig.addDataSourceProperty( "cachePrepStmts" , "true" ); //pozwala lepiej wspolpracowac z prepared statements
        hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setMaximumPoolSize(30);
        hikari = new HikariDataSource(hikariConfig);
    }
}
