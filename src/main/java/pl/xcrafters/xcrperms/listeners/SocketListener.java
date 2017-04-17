package pl.xcrafters.xcrperms.listeners;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.xcrafters.xcrperms.data.DataGroup;
import pl.xcrafters.xcrperms.data.DataUser;
import pl.xcrafters.xcrperms.PermsPlugin;
import pl.xcrafters.xcrperms.data.DataInheritance;
import pl.xcrafters.xcrperms.data.DataManager.PermissionType;
import pl.xcrafters.xcrperms.data.DataPermission;

public class SocketListener extends Thread {

    PermsPlugin plugin;

    ServerSocket server;

    public SocketListener(PermsPlugin plugin) {
        this.plugin = plugin;
        try {
            server = new ServerSocket(Bukkit.getPort() - 29080 + 41000);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                if (server != null && !server.isClosed()) {
                    Socket receive = server.accept();
                    DataInputStream dis = new DataInputStream(receive.getInputStream());
                    String channel = dis.readUTF();
                    if (channel.equals("InsertUser")) {
                        int id = dis.readInt();
                        DataUser user = plugin.mysqlManager.loadUser(id);
                        if (Bukkit.getPlayerExact(user.getNickname()) != null) {
                            plugin.dataManager.users.put(user.getNickname().toLowerCase(), user);
                            Player player = Bukkit.getPlayerExact(user.getNickname());
                            player.recalculatePermissions();
                        }
                    }
                    if (channel.equals("InsertGroup")) {
                        int id = dis.readInt();
                        DataGroup group = plugin.mysqlManager.loadGroup(id);
                        plugin.dataManager.groups.put(group.getGroupName().toLowerCase(), group);
                    }
                    if (channel.equals("InsertPermission")) {
                        int id = dis.readInt();
                        DataPermission perm = plugin.mysqlManager.loadPermission(id);
                        if (perm.getType().equals(PermissionType.GROUP)) {
                            plugin.dataManager.permissions.add(perm);
                            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                public void run() {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        plugin.recalculatePermissions(player);
                                    }
                                }
                            }, 1L);
                        } else {
                            DataUser user = plugin.dataManager.getUserById(perm.getTypeID());
                            if (user != null) {
                                plugin.dataManager.permissions.add(perm);
                                final Player player = Bukkit.getPlayerExact(user.getNickname());
                                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                    public void run() {
                                        plugin.recalculatePermissions(player);
                                    }
                                }, 1L);
                            }
                        }
                    }
                    if (channel.equals("InsertInheritance")) {
                        int id = dis.readInt();
                        final DataInheritance inherit = plugin.mysqlManager.loadInheritance(id);
                        plugin.dataManager.inheritances.add(inherit);
                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                            public void run() {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    plugin.recalculatePermissions(player);
                                }
                            }
                        }, 1L);
                    }
                    if (channel.equals("UpdateUser")) {
                        int id = dis.readInt();
                        final DataUser user = plugin.dataManager.getUserById(id);
                        if (user != null) {
                            user.update();
                            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                public void run() {
                                    Player player = Bukkit.getPlayerExact(user.getNickname());
                                    if(player != null){
                                        plugin.recalculatePermissions(player);
                                    }
                                }
                            }, 1L);
                        }
                    }
                    if (channel.equals("UpdateGroup")) {
                        int id = dis.readInt();
                        DataGroup group = plugin.dataManager.getGroupById(id);
                        group.update();
                    }
                    if(channel.equals("UpdatePermission")){
                        int id = dis.readInt();
                        DataPermission perm = plugin.dataManager.getPermissionById(id);
                        perm.update();
                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                            public void run() {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    plugin.recalculatePermissions(player);
                                }
                            }
                        }, 1L);
                    }
                    if (channel.equals("DeleteUser")) {
                        int id = dis.readInt();
                        DataUser user = plugin.dataManager.getUserById(id);
                        if (user != null) {
                            plugin.dataManager.users.remove(user.getNickname().toLowerCase());
                        }
                    }
                    if (channel.equals("DeleteGroup")) {
                        int id = dis.readInt();
                        DataGroup group = plugin.dataManager.getGroupById(id);
                        plugin.dataManager.groups.remove(group.getGroupName().toLowerCase());
                    }
                    if (channel.equals("DeletePermission")) {
                        int id = dis.readInt();
                        DataPermission perm = plugin.dataManager.getPermissionById(id);
                        if (perm.getType().equals(PermissionType.GROUP)) {
                            plugin.dataManager.permissions.remove(perm);
                            final DataGroup group = plugin.dataManager.getGroupById(perm.getTypeID());
                            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                public void run() {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        plugin.recalculatePermissions(player);
                                    }
                                }
                            }, 1L);
                        } else {
                            DataUser user = plugin.dataManager.getUserById(perm.getTypeID());
                            if (user != null) {
                                plugin.dataManager.permissions.remove(perm);
                                final Player player = Bukkit.getPlayerExact(user.getNickname());
                                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                    public void run() {
                                        plugin.recalculatePermissions(player);
                                    }
                                }, 1L);
                            }
                        }
                        plugin.dataManager.permissions.remove(perm);
                    }
                    if (channel.equals("DeleteInheritance")) {
                        int id = dis.readInt();
                        final DataInheritance inherit = plugin.dataManager.getInheritanceById(id);
                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                            public void run() {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    plugin.recalculatePermissions(player);
                                }
                            }
                        }, 1L);
                        plugin.dataManager.inheritances.remove(inherit);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    public synchronized void close() throws IOException {
        server.close();
    }

}
