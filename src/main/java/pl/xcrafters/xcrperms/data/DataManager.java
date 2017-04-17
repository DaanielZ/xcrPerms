package pl.xcrafters.xcrperms.data;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import pl.xcrafters.xcrperms.PermsPlugin;

public class DataManager implements Listener{

    PermsPlugin plugin;
    
    public ConcurrentHashMap<String, DataUser> users = new ConcurrentHashMap();
    public ConcurrentHashMap<UUID, DataUser> usersByUUID = new ConcurrentHashMap();
    public ConcurrentHashMap<String, DataGroup> groups = new ConcurrentHashMap();
    public List<DataPermission> permissions = new CopyOnWriteArrayList();
    public List<DataInheritance> inheritances = new CopyOnWriteArrayList();
    
    public enum PermissionType {USER, GROUP}
    
    public ConcurrentHashMap<String, PermissionAttachment> attachments = new ConcurrentHashMap();
    
    public DataManager(PermsPlugin plugin){
        this.plugin = plugin;
    }
    
    public DataUser createUser(String nickname, UUID uuid){
        return new DataUser(plugin, nickname, uuid);
    }
    
    public DataUser getUserByPlayer(Player player){
        return getUserByNick(player.getName());
    }
    
    public DataUser getUserByNick(String nick){
        return users.get(nick.toLowerCase());
    }
    
    public DataUser getUserById(int id){
        for(DataUser user : users.values()){
            if(user.getPrimary() == id){
                return user;
            }
        }
        return null;
    }
    
    public DataGroup getGroupByName(String groupName){
        return groups.get(groupName.toLowerCase());
    }
    
    public DataGroup getGroupById(int id){
        for(DataGroup group : groups.values()){
            if(group.getPrimary() == id){
                return group;
            }
        }
        return null;
    }
    
    public DataPermission getPermissionById(int id){
        for(DataPermission perm : permissions){
            if(perm.getPrimary() == id){
                return perm;
            }
        }
        return null;
    }
    
    public DataInheritance getInheritanceById(int id){
        for(DataInheritance inherit : inheritances){
            if(inherit.getPrimary() == id){
                return inherit;
            }
        }
        return null;
    }
    
}
