package pl.xcrafters.xcrperms.listeners;

import org.bukkit.event.Listener;
import pl.xcrafters.xcrperms.PermsPlugin;

public class PlayerQuitListener implements Listener{

    PermsPlugin plugin;
    
    public PlayerQuitListener(PermsPlugin plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
//    @EventHandler
//    public void onPlayerQuit(PlayerQuitEvent event){
//        Player player = event.getPlayer();
//        if(plugin.dataManager.attachments.get(player.getName()) != null){
//            player.removeAttachment(plugin.dataManager.attachments.get(player.getName()));
//        }
//        DataUser user = plugin.dataManager.getUserByPlayer(player);
//        if(user != null){
//            plugin.dataManager.users.remove(user.getNickname().toLowerCase());
//        }
//    }
//
//    @EventHandler
//    public void onPlayerKick(PlayerKickEvent event){
//        Player player = event.getPlayer();
//        if(plugin.dataManager.attachments.get(player.getName()) != null){
//            player.removeAttachment(plugin.dataManager.attachments.get(player.getName()));
//        }
//        DataUser user = plugin.dataManager.getUserByPlayer(player);
//        if(user != null){
//            plugin.dataManager.users.remove(user.getNickname().toLowerCase());
//        }
//    }
    
}
