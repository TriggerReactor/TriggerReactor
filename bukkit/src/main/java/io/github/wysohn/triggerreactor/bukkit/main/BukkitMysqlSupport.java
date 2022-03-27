package io.github.wysohn.triggerreactor.bukkit.main;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import io.github.wysohn.triggerreactor.bukkit.scope.JavaPluginLifetime;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.tools.mysql.MiniConnectionPoolManager;

import javax.inject.Inject;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

@JavaPluginLifetime
public class BukkitMysqlSupport extends Manager {
    @Inject
    Logger logger;
    @Inject
    PluginConfigManager configManager;

    private String address;
    private String dbName;
    private String tableName;

    private MysqlConnectionPoolDataSource ds;
    private MiniConnectionPoolManager pool;

    @Inject
    BukkitMysqlSupport(){

    }

    private Connection createConnection() {
        Connection conn = null;

        try {
            conn = pool.getConnection();
        } catch (SQLException e) {
            // e.printStackTrace();
        } finally {
            if (conn == null)
                conn = pool.getValidConnection();
        }

        return conn;
    }

    private void initTable(Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(String.format(CREATETABLEQUARY, tableName));
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public String toString() {
        return "Mysql Connection(" + address + ") to [dbName=" + dbName + ", tablename=" + tableName + "]";
    }

    public Object get(String key) throws SQLException {
        Object out = null;

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT " + VALUE + " FROM " + tableName + " WHERE " + KEY + " = ?")) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next())
                return null;
            InputStream is = rs.getBinaryStream(VALUE);

            try (ObjectInputStream ois = new ObjectInputStream(is)) {
                out = ois.readObject();
            } catch (IOException | ClassNotFoundException e1) {
                e1.printStackTrace();
                return null;
            }
        }

        return out;
    }

    public void set(String key, Serializable value) throws SQLException {
        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement("REPLACE INTO " + tableName + " VALUES (?, ?)")) {


            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(value);

                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                pstmt.setString(1, key);
                pstmt.setBinaryStream(2, bais);

                pstmt.executeUpdate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() throws RuntimeException {
        logger.info("Initializing Mysql support...");

        if (!configManager.get("Mysql.Enable", Boolean.class).orElse(false)) {
            String path = "Mysql.Enable";
            if (!configManager.has(path))
                configManager.put(path, false);
            path = "Mysql.Address";
            if (!configManager.has(path))
                configManager.put(path, "127.0.0.1:3306");
            path = "Mysql.DbName";
            if (!configManager.has(path))
                configManager.put(path, "TriggerReactor");
            path = "Mysql.UserName";
            if (!configManager.has(path))
                configManager.put(path, "root");
            path = "Mysql.Password";
            if (!configManager.has(path))
                configManager.put(path, "1234");
        }

        address = configManager.get("Mysql.Address", String.class).orElse(null);
        dbName = configManager.get("Mysql.DbName", String.class).orElse(null);
        tableName = "data";
        String userName = configManager.get("Mysql.UserName", String.class).orElse(null);
        String password = configManager.get("Mysql.Password", String.class).orElse(null);

        ds = new MysqlConnectionPoolDataSource();
        ds.setURL("jdbc:mysql://" + address + "/" + dbName);
        ds.setUser(userName);
        ds.setPassword(password);
        ds.setCharacterEncoding("UTF-8");
        ds.setUseUnicode(true);
        ds.setAutoReconnectForPools(true);
        ds.setAutoReconnect(true);
        ds.setAutoReconnectForConnectionPools(true);

        ds.setCachePreparedStatements(true);
        ds.setCachePrepStmts(true);

        pool = new MiniConnectionPoolManager(ds, 2);

        Connection conn = createConnection();
        try{
            initTable(conn);
            conn.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        logger.info("Done!");
    }

    private static final String KEY = "dbkey";
    private static final String VALUE = "dbval";
    private static final String CREATETABLEQUARY =
            "CREATE TABLE IF NOT EXISTS %s (" + KEY + " CHAR(128) PRIMARY KEY," + VALUE
                    + " MEDIUMBLOB)";
}
