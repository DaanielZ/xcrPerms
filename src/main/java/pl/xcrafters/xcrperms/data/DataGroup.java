package pl.xcrafters.xcrperms.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.xcrafters.xcrperms.PermsPlugin;
import pl.xcrafters.xcrperms.data.DataManager.PermissionType;

public class DataGroup implements DataInterface{

    PermsPlugin plugin;
    
    public DataGroup(PermsPlugin plugin, ResultSet rs){
        this.plugin = plugin;
        try {
            this.id = rs.getInt("groupID");
            this.groupName = rs.getString("groupName");
        } catch (SQLException ex) { }
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
    
    private String groupName;
    
    public String getGroupName(){ return groupName; }
    public final Map<String, Boolean> getGroupPermissions(){
        Map<String, Boolean> perms = new HashMap();
        for(DataPermission perm : plugin.dataManager.permissions){
            if(perm.getType().equals(PermissionType.GROUP) && perm.getTypeID() == id){
                perms.put(perm.getPermission(), perm.getValue());
            }
        }
        return perms;
    }
    public final List<DataGroup> getInheritGroups(){
        List<DataGroup> groups = new ArrayList();
        for(DataInheritance inherit : plugin.dataManager.inheritances){
            if(inherit.getGroup().equals(this)){
                groups.add(inherit.getSub());
            }
        }
        return groups;
    }
    public final Map<String, Boolean> getPermissions() {
        Map<String, Boolean> perms = new HashMap();
        for (DataGroup inherit : getInheritGroups()) {
            for (Entry<String, Boolean> perm : inherit.getPermissions().entrySet()) {
                perms.put(perm.getKey(), perm.getValue());
            }
        }
        for(Entry<String, Boolean> perm : getGroupPermissions().entrySet()){
            perms.put(perm.getKey(), perm.getValue());
        }
        return perms;
    }
    
    @Override
    public void update(){
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Groups WHERE groupID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.groupName = rs.getString("groupName");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
