package pl.xcrafters.xcrperms.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.xcrafters.xcrperms.PermsPlugin;
import pl.xcrafters.xcrperms.data.DataManager.PermissionType;

public class DataUser implements DataInterface {

    PermsPlugin plugin;
    
    public DataUser(PermsPlugin plugin, ResultSet rs){
        try {
            this.plugin = plugin;
            this.id = rs.getInt("userID");
            this.nickname = rs.getString("nickname");
            this.uuid = rs.getString("uuid") != null ? UUID.fromString(rs.getString("uuid")) : null;
            this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DataUser(PermsPlugin plugin, String nickname, UUID uuid){
        this.plugin = plugin;
        this.nickname = nickname;
        this.uuid = uuid;
        this.group = plugin.dataManager.getGroupByName("default");
    }
    
    private int id;
    
    @Override
    public void setPrimary(int id){
        this.id = id;
    }
    
    @Override
    public int getPrimary(){
        return id;
    }
    
    private String nickname;
    private UUID uuid;
    private DataGroup group;

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setUUID(UUID uuid) { this.uuid = uuid; }
    public void setGroup(DataGroup group) { this.group = group; }
    
    public String getNickname() { return nickname; }
    public UUID getUUID() { return uuid; }
    public final Map<String, Boolean> getPermissions() {
        Map<String, Boolean> perms = new HashMap();
        for(DataPermission perm : plugin.dataManager.permissions){
            if(perm.getType().equals(PermissionType.USER) && perm.getTypeID() == id){
                perms.put(perm.getPermission(), perm.getValue());
            }
        }
        return perms;
    }
    public DataGroup getGroup() { return group; }
    
    @Override
    public void update(){
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE userID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.nickname = rs.getString("nickname");
                this.uuid = rs.getString("uuid") != null ? UUID.fromString(rs.getString("uuid")) : null;
                this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
