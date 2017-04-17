package pl.xcrafters.xcrperms;

import org.bukkit.entity.Player;
import pl.xcrafters.xcrperms.data.DataUser;

public class PermsAPI {

    static PermsPlugin plugin;

    public PermsAPI(PermsPlugin permsPlugin) {
        plugin = permsPlugin;
    }

    public static String getPlayerGroup(Player player) {
        DataUser user = plugin.dataManager.getUserByPlayer(player);
        if(user == null || user.getGroup() == null) {
            return "default";
        }
        return user.getGroup().getGroupName();
    }

}
