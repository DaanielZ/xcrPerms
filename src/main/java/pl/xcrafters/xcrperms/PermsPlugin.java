package pl.xcrafters.xcrperms;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import pl.xcrafters.xcrperms.data.DataGroup;
import pl.xcrafters.xcrperms.data.DataUser;
import pl.xcrafters.xcrperms.listeners.AsyncPlayerPreLoginListener;
import pl.xcrafters.xcrperms.listeners.PlayerLoginListener;
import pl.xcrafters.xcrperms.listeners.PlayerQuitListener;
import pl.xcrafters.xcrperms.data.DataManager;
import pl.xcrafters.xcrperms.listeners.RedisListener;
import pl.xcrafters.xcrperms.mysql.MySQLManager;
import pl.xcrafters.xcrperms.redis.RedisManager;

public class PermsPlugin extends JavaPlugin {

    public ConfigManager configManager;
    public DataManager dataManager;
    public MySQLManager mysqlManager;
    public RedisManager redisManager;

    AsyncPlayerPreLoginListener asyncPlayerPreLoginListener;
    PlayerLoginListener playerJoinListener;
    PlayerQuitListener playerQuitListener;
    RedisListener redisListener;

    PermsAPI api;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.mysqlManager = new MySQLManager(this);
        this.redisManager = new RedisManager(this);

        this.asyncPlayerPreLoginListener = new AsyncPlayerPreLoginListener(this);
        this.playerJoinListener = new PlayerLoginListener(this);
        this.playerQuitListener = new PlayerQuitListener(this);
        this.redisListener = new RedisListener(this);

        this.api = new PermsAPI(this);

        for(Player player : Bukkit.getOnlinePlayers()) {
            DataUser user = mysqlManager.loadUser(player.getUniqueId());
            if(user != null && !user.getNickname().equals(player.getName())) {
                user.setNickname(player.getName());
            }
            if(user == null) {
                user = mysqlManager.loadUser(player.getName());
                if (user != null && user.getUUID() == null) {
                    user.setUUID(player.getUniqueId());
                }
                if (user == null) {
                    user = dataManager.createUser(player.getName(), player.getUniqueId());
                }
            }

            dataManager.users.put(user.getNickname().toLowerCase(), user);
            dataManager.usersByUUID.put(user.getUUID(), user);

            PermissionAttachment pat = player.addAttachment(this);
            Map<String, Boolean> perms = new LinkedHashMap<String, Boolean>();
            if (user == null) {
                DataGroup group = dataManager.getGroupByName("default");
                for (Entry<String, Boolean> perm : group.getPermissions().entrySet()) {
                    perms.put(perm.getKey(), perm.getValue());
                }
            } else {
                if (user.getGroup() != null) {
                    for (Entry<String, Boolean> perm : user.getGroup().getPermissions().entrySet()) {
                        if(perms.get(perm.getKey()) == null || (perms.get(perm.getKey()) != null && !perms.get(perm.getKey()))) {
                            perms.put(perm.getKey(), perm.getValue());
                        }
                    }
                } else if (dataManager.getGroupByName("default") != null) {
                    for (Entry<String, Boolean> perm : dataManager.getGroupByName("default").getPermissions().entrySet()) {
                        perms.put(perm.getKey(), perm.getValue());
                    }
                }
                for (Entry<String, Boolean> perm : user.getPermissions().entrySet()) {
                    if(perms.get(perm.getKey()) == null || (perms.get(perm.getKey()) != null && !perms.get(perm.getKey()))) {
                        perms.put(perm.getKey(), perm.getValue());
                    }
                }
            }
            Map<String, Boolean> map = reflectMap(pat);
            map.clear();
            map.putAll(perms);
            player.recalculatePermissions();
            dataManager.attachments.put(player.getName(), pat);
        }
    }

    @Override
    public void onDisable() {
        mysqlManager.closeConnection();
    }

    private Field pField;

    @SuppressWarnings("unchecked")
    public Map<String, Boolean> reflectMap(PermissionAttachment attachment) {
        try {
            if (pField == null) {
                pField = PermissionAttachment.class.getDeclaredField("permissions");
                pField.setAccessible(true);
            }
            return (Map<String, Boolean>) pField.get(attachment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void recalculatePermissions(Player player) {
        PermissionAttachment pat = dataManager.attachments.get(player.getName());
        DataUser user = dataManager.getUserByPlayer(player);
        Map<String, Boolean> perms = new LinkedHashMap<String, Boolean>();
        if (user == null) {
            DataGroup group = dataManager.getGroupByName("default");
            if(group != null) {
                for (Entry<String, Boolean> perm : group.getPermissions().entrySet()) {
                    perms.put(perm.getKey(), perm.getValue());
                }
            }
        } else {
            if (user.getGroup() != null) {
                for (Entry<String, Boolean> perm : user.getGroup().getPermissions().entrySet()) {
                    if(perms.get(perm.getKey()) == null || (perms.get(perm.getKey()) != null && !perms.get(perm.getKey()))) {
                        perms.put(perm.getKey(), perm.getValue());
                    }
                }
            } else if (dataManager.getGroupByName("default") != null) {
                for (Entry<String, Boolean> perm : dataManager.getGroupByName("default").getPermissions().entrySet()) {
                    perms.put(perm.getKey(), perm.getValue());
                }
            }
            for (Entry<String, Boolean> perm : user.getPermissions().entrySet()) {
                if(perms.get(perm.getKey()) == null || (perms.get(perm.getKey()) != null && !perms.get(perm.getKey()))) {
                    perms.put(perm.getKey(), perm.getValue());
                }
            }
        }
        Map<String, Boolean> map = reflectMap(pat);
        map.clear();
        map.putAll(perms);
        player.recalculatePermissions();
    }

}
