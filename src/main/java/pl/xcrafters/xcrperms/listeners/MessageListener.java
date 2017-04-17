package pl.xcrafters.xcrperms.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import pl.xcrafters.xcrperms.PermsPlugin;
import pl.xcrafters.xcrperms.data.*;

public class MessageListener implements PluginMessageListener {

    PermsPlugin plugin;

    public MessageListener(PermsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player receiver, byte[] message) {
        if(!channel.equals("BungeeCord")) {
            return;
        }
        final ByteArrayDataInput in = ByteStreams.newDataInput(message);
        final String subchannel = in.readUTF();
        if (subchannel.equals("InsertUser")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
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
            });
        }
        if (subchannel.equals("InsertGroup")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    DataGroup group = plugin.mysqlManager.loadGroup(id);
                    plugin.dataManager.groups.put(group.getGroupName().toLowerCase(), group);
                }
            });
        }
        if (subchannel.equals("InsertPermission")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
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
            });
        }
        if (subchannel.equals("InsertInheritance")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
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
            });
        }
        if (subchannel.equals("UpdateUser")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    DataUser user = plugin.dataManager.getUserById(id);
                    if(user == null) {
                        user = plugin.mysqlManager.loadUser(id);
                        if (Bukkit.getPlayerExact(user.getNickname()) != null) {
                            plugin.dataManager.users.put(user.getNickname().toLowerCase(), user);
                            plugin.dataManager.usersByUUID.put(user.getUUID(), user);
                        }
                    } else {
                        user.update();
                    }
                    final Player player = Bukkit.getPlayerExact(user.getNickname());
                    if (player != null) {
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.recalculatePermissions(player);
                            }
                        });
                    }
                }
            });
        }
        if (subchannel.equals("UpdateGroup")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    DataGroup group = plugin.dataManager.getGroupById(id);
                    group.update();
                }
            });
        }
        if(subchannel.equals("UpdatePermission")){
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
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
            });
        }
        if (subchannel.equals("DeleteUser")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    DataUser user = plugin.dataManager.getUserById(id);
                    if (user != null) {
                        plugin.dataManager.users.remove(user.getNickname().toLowerCase());
                    }
                }
            });
        }
        if (subchannel.equals("DeleteGroup")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    DataGroup group = plugin.dataManager.getGroupById(id);
                    plugin.dataManager.groups.remove(group.getGroupName().toLowerCase());
                }
            });
        }
        if (subchannel.equals("DeletePermission")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
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
            });
        }
        if (subchannel.equals("DeleteInheritance")) {
            final int id = in.readInt();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

}
