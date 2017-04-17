package pl.xcrafters.xcrperms.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.xcrafters.xcrperms.PermsPlugin;

public class DataInheritance implements DataInterface{

    PermsPlugin plugin;
    
    public DataInheritance(PermsPlugin plugin, ResultSet rs){
        this.plugin = plugin;
        try {
            this.id = rs.getInt("inheritID");
            this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
            this.sub = plugin.dataManager.getGroupById(rs.getInt("subID"));
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
    
    private DataGroup group;
    private DataGroup sub;
    
    public DataGroup getGroup(){ return group; }
    public DataGroup getSub(){ return sub; }
    
    @Override
    public void update(){
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Inheritances WHERE inheritID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
                this.sub = plugin.dataManager.getGroupById(rs.getInt("subID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
