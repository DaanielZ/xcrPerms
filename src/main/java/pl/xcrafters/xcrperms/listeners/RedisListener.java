package pl.xcrafters.xcrperms.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.xcrafters.xcrperms.PermsPlugin;
import pl.xcrafters.xcrperms.data.*;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

public class RedisListener extends JedisPubSub {

    PermsPlugin plugin;

    public RedisListener(PermsPlugin plugin) {
        this.plugin = plugin;
        plugin.redisManager.subscribe(this, "PermInsertUser", "PermInsertGroup", "PermInsertPermission", "PermInsertInheritance", "PermUpdateUser", "PermUpdateGroup", "PermUpdatePermission", "PermUpdateInheritance", "PermDeleteUser", "PermDeleteGroup", "PermDeletePermission", "PermDeleteInheritance");
    }

    Gson gson = new Gson();

    public void onMessage(String channel, final String json) {
        try {
            JsonObject object = gson.fromJson(json, JsonObject.class);

            int id = object.get("id").getAsInt();

            if (channel.equals("PermInsertUser")) {
                DataUser user = plugin.mysqlManager.loadUser(id);
                if (Bukkit.getPlayerExact(user.getNickname()) != null) {
                    plugin.dataManager.users.put(user.getNickname().toLowerCase(), user);
                    plugin.dataManager.usersByUUID.put(user.getUUID(), user);
                    final Player player = Bukkit.getPlayerExact(user.getNickname());
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            player.recalculatePermissions();
                        }
                    });
                }
            }
            if (channel.equals("PermInsertGroup")) {
                DataGroup group = plugin.mysqlManager.loadGroup(id);
                plugin.dataManager.groups.put(group.getGroupName().toLowerCase(), group);
            }
            if (channel.equals("PermInsertPermission")) {
                DataPermission perm = plugin.mysqlManager.loadPermission(id);
                if (perm.getType().equals(DataManager.PermissionType.GROUP)) {
                    plugin.dataManager.permissions.add(perm);
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                plugin.recalculatePermissions(player);
                            }
                        }
                    });
                } else {
                    DataUser user = plugin.dataManager.getUserById(perm.getTypeID());
                    if (user != null) {
                        plugin.dataManager.permissions.add(perm);
                        final Player player = Bukkit.getPlayerExact(user.getNickname());
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.recalculatePermissions(player);
                            }
                        });
                    }
                }
            }
            if (channel.equals("PermInsertInheritance")) {
                DataInheritance inherit = plugin.mysqlManager.loadInheritance(id);
                plugin.dataManager.inheritances.add(inherit);
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            plugin.recalculatePermissions(player);
                        }
                    }
                });
            }
            if (channel.equals("PermUpdateUser")) {
                DataUser user = plugin.dataManager.getUserById(id);
                if(user != null) {
                    user.update();
                    final Player player = Bukkit.getPlayerExact(user.getNickname());
                    if (player != null) {
                        plugin.getLogger().log(Level.INFO, player.getName());
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.recalculatePermissions(player);
                            }
                        });
                    }
                }
            }
            if (channel.equals("PermUpdateGroup")) {
                DataGroup group = plugin.dataManager.getGroupById(id);
                group.update();
            }
            if(channel.equals("PermUpdatePermission")){
                DataPermission perm = plugin.dataManager.getPermissionById(id);
                perm.update();
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            plugin.recalculatePermissions(player);
                        }
                    }
                });
            }
            if (channel.equals("PermDeleteUser")) {
                DataUser user = plugin.dataManager.getUserById(id);

                if (user != null) {
                    plugin.dataManager.users.remove(user.getNickname().toLowerCase());
                }

                final Player player = Bukkit.getPlayerExact(user.getNickname());
                if (player != null) {
                    plugin.getLogger().log(Level.INFO, player.getName());
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            plugin.recalculatePermissions(player);
                        }
                    });
                }
            }
            if (channel.equals("PermDeleteGroup")) {
                DataGroup group = plugin.dataManager.getGroupById(id);
                plugin.dataManager.groups.remove(group.getGroupName().toLowerCase());
            }
            if (channel.equals("PermDeletePermission")) {
                DataPermission perm = plugin.dataManager.getPermissionById(id);
                if (perm.getType().equals(DataManager.PermissionType.GROUP)) {
                    plugin.dataManager.permissions.remove(perm);
                    DataGroup group = plugin.dataManager.getGroupById(perm.getTypeID());
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                plugin.recalculatePermissions(player);
                            }
                        }
                    });
                } else {
                    DataUser user = plugin.dataManager.getUserById(perm.getTypeID());
                    if (user != null) {
                        plugin.dataManager.permissions.remove(perm);
                        final Player player = Bukkit.getPlayerExact(user.getNickname());
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.recalculatePermissions(player);
                            }
                        });
                    }
                }
                plugin.dataManager.permissions.remove(perm);
            }
            if (channel.equals("PermDeleteInheritance")) {
                DataInheritance inherit = plugin.dataManager.getInheritanceById(id);
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            plugin.recalculatePermissions(player);
                        }
                    }
                });
                plugin.dataManager.inheritances.remove(inherit);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSubscribe(String channel, int subscribedChannels) {
    }

    public void onUnsubscribe(String channel, int subscribedChannels) {
    }

    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    public void onPMessage(String pattern, String channel, String message) {
    }


}
