package pl.xcrafters.xcrperms;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    PermsPlugin plugin;
    FileConfiguration config;
    
    public ConfigManager(PermsPlugin plugin){
        this.plugin = plugin;
        this.config = plugin.getConfig();
        plugin.saveDefaultConfig();
        load();
    }
    
    public String mysqlHost;
    public String mysqlBase;
    public String mysqlUser;
    public String mysqlPass;

    public String redisHost;
    
    public void load(){
        mysqlHost = config.getString("config.mysql.host");
        mysqlBase = config.getString("config.mysql.base");
        mysqlUser = config.getString("config.mysql.user");
        mysqlPass = config.getString("config.mysql.pass");

        redisHost = config.getString("config.redis.host");
    }
    
}
