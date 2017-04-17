package pl.xcrafters.xcrperms.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.xcrafters.xcrperms.PermsPlugin;
import pl.xcrafters.xcrperms.data.DataGroup;
import pl.xcrafters.xcrperms.data.DataInheritance;
import pl.xcrafters.xcrperms.data.DataPermission;
import pl.xcrafters.xcrperms.data.DataUser;

public class MySQLManager {

    PermsPlugin plugin;
    
    private Connection conn;
    
    public MySQLManager(PermsPlugin plugin){
        this.plugin = plugin;
        conn = prepareConnection();
        loadAll();
    }
    
    private Connection prepareConnection() {
        for (int i = 0; i < 5; i++) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://" + plugin.configManager.mysqlHost + "/" + plugin.configManager.mysqlBase;
                return DriverManager.getConnection(url, plugin.configManager.mysqlUser, plugin.configManager.mysqlPass);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "CANNOT CONNECT TO DATABASE!", ex);
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().log(Level.SEVERE, "JDBC IS NOT FOUND - CANNOT CONNECT TO DATABASE!", ex);
            }
        }
        return null;
    }

    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = prepareConnection();
        }
        return conn;
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadAll(){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Groups");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DataGroup group = new DataGroup(plugin, rs);
                plugin.dataManager.groups.put(group.getGroupName().toLowerCase(), group);
            }
            ps = conn.prepareStatement("SELECT * FROM Permissions");
            rs = ps.executeQuery();
            while (rs.next()) {
                DataPermission perm = new DataPermission(plugin, rs);
                plugin.dataManager.permissions.add(perm);
            }
            ps = conn.prepareStatement("SELECT * FROM Inheritances");
            rs = ps.executeQuery();
            while (rs.next()) {
                DataInheritance inherit = new DataInheritance(plugin, rs);
                plugin.dataManager.inheritances.add(inherit);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DataUser loadUser(String nick){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE nickname='" + nick + "'");
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataUser user = new DataUser(plugin, rs);
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public DataUser loadUser(UUID uuid){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE uuid='" + uuid.toString() + "'");
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataUser user = new DataUser(plugin, rs);
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public DataUser loadUser(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE userID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataUser user = new DataUser(plugin, rs);
                return user;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public DataGroup loadGroup(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Groups WHERE groupID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataGroup group = new DataGroup(plugin, rs); 
                return group;
            }
        } catch(SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public DataPermission loadPermission(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Permissions WHERE permID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataPermission perm = new DataPermission(plugin, rs); 
                return perm;
            }
        } catch(SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public DataInheritance loadInheritance(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Inheritances WHERE inheritID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataInheritance inherit = new DataInheritance(plugin, rs); 
                return inherit;
            }
        } catch(SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
