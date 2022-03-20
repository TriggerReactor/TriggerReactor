package io.github.wysohn.triggerreactor.bukkit.main;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import io.github.wysohn.triggerreactor.tools.mysql.MiniConnectionPoolManager;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BukkitMysqlSupport {
    private final String KEY = "dbkey";
    private final String VALUE = "dbval";

    private final MysqlConnectionPoolDataSource ds;
    private final MiniConnectionPoolManager pool;

    private final String dbName;
    private final String tablename;

    private final String address;
    private final String CREATETABLEQUARY =
            "CREATE TABLE IF NOT EXISTS %s (" + KEY + " CHAR(128) PRIMARY KEY," + VALUE
                    + " MEDIUMBLOB)";

    BukkitMysqlSupport(String address, String dbName, String tablename, String userName, String password) throws
            SQLException {
        this.dbName = dbName;
        this.tablename = tablename;
        this.address = address;

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
        initTable(conn);
        conn.close();
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
        PreparedStatement pstmt = conn.prepareStatement(String.format(CREATETABLEQUARY, tablename));
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public String toString() {
        return "Mysql Connection(" + address + ") to [dbName=" + dbName + ", tablename=" + tablename + "]";
    }

    public Object get(String key) throws SQLException {
        Object out = null;

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT " + VALUE + " FROM " + tablename + " WHERE " + KEY + " = ?")) {
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
             PreparedStatement pstmt = conn.prepareStatement("REPLACE INTO " + tablename + " VALUES (?, ?)")) {


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
}
