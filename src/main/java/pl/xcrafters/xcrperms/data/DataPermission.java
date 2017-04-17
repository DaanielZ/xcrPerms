package pl.xcrafters.xcrperms.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.xcrafters.xcrperms.PermsPlugin;

public class DataPermission implements DataInterface{

    PermsPlugin plugin;
    
    public DataPermission(PermsPlugin plugin, ResultSet rs){
        this.plugin = plugin;
        try {
            this.id = rs.getInt("permID");
            this.permission = rs.getString("permission");
            this.type = DataManager.PermissionType.valueOf(rs.getString("type").toUpperCase());
            this.typeID = rs.getInt("typeID");
            this.value = rs.getInt("value") == 1;
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
    
    private String permission;
    private DataManager.PermissionType type;
    private int typeID;
    private boolean value;

    public String getPermission(){ return permission; }
    public DataManager.PermissionType getType(){ return type; }
    public int getTypeID(){ return typeID; }
    public boolean getValue(){ return value; }

    @Override
    public void update() {
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Permissions WHERE permID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.permission = rs.getString("permission");
                this.type = DataManager.PermissionType.valueOf(rs.getString("type").toUpperCase());
                this.typeID = rs.getInt("typeID");
                this.value = rs.getInt("value") == 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
