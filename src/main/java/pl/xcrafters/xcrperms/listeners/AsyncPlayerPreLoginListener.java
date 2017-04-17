package pl.xcrafters.xcrperms.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import pl.xcrafters.xcrperms.data.DataUser;
import pl.xcrafters.xcrperms.PermsPlugin;

import java.util.UUID;

public class AsyncPlayerPreLoginListener implements Listener{

    PermsPlugin plugin;
    
    public AsyncPlayerPreLoginListener(PermsPlugin plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event){
        String nick = event.getName();
        UUID uuid = event.getUniqueId();
        DataUser user = plugin.mysqlManager.loadUser(uuid);
        if(user != null && !user.getNickname().equals(nick)) {
            user.setNickname(nick);
        }
        if(user == null) {
            user = plugin.mysqlManager.loadUser(nick);
            if (user != null && user.getUUID() == null) {
                user.setUUID(uuid);
            }
            if (user == null) {
                user = plugin.dataManager.createUser(event.getName(), event.getUniqueId());
            }
        }
        plugin.dataManager.usersByUUID.put(user.getUUID(), user);
        plugin.dataManager.users.put(user.getNickname().toLowerCase(), user);
    }
    
}
