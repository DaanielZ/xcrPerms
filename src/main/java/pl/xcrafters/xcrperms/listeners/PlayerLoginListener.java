package pl.xcrafters.xcrperms.listeners;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.permissions.PermissionAttachment;
import pl.xcrafters.xcrperms.PermsPlugin;
import pl.xcrafters.xcrperms.data.DataGroup;
import pl.xcrafters.xcrperms.data.DataUser;

public class PlayerLoginListener implements Listener {

    PermsPlugin plugin;

    public PlayerLoginListener(PermsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Field pField;

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult().equals(Result.ALLOWED)) {
            Player player = event.getPlayer();
            PermissionAttachment pat = player.addAttachment(plugin);
            DataUser user = plugin.dataManager.getUserByPlayer(player);
            Map<String, Boolean> perms = new LinkedHashMap<String, Boolean>();
            if (user == null) {
                DataGroup group = plugin.dataManager.getGroupByName("default");
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
                } else if (plugin.dataManager.getGroupByName("default") != null) {
                    for (Entry<String, Boolean> perm : plugin.dataManager.getGroupByName("default").getPermissions().entrySet()) {
                        perms.put(perm.getKey(), perm.getValue());
                    }
                }
                for (Entry<String, Boolean> perm : user.getPermissions().entrySet()) {
                    if(perms.get(perm.getKey()) == null || (perms.get(perm.getKey()) != null && !perms.get(perm.getKey()))) {
                        perms.put(perm.getKey(), perm.getValue());
                    }
                }
            }
            Map<String, Boolean> map = plugin.reflectMap(pat);
            map.clear();
            map.putAll(perms);
            player.recalculatePermissions();
            plugin.dataManager.attachments.put(player.getName(), pat);
        }
    }

}
