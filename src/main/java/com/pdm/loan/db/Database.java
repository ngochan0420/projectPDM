package com.pdm.loan.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Database {

    private static final Properties CONFIG = loadConfig();

    private Database() {
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                p.load(in);
            }
            Path external = Path.of("db.properties");
            if (Files.exists(external)) {
                try (InputStream ext = Files.newInputStream(external)) {
                    p.load(ext);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read db.properties", e);
        }
        for (String key : List.of("db.url", "db.user", "db.password")) {
            String value = System.getProperty(key);
            if (value != null) {
                p.setProperty(key, value);
            }
        }
        return p;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    // ------------------------------------------------------------------ SELECT

    public static TableData query(String sql, Object... params) throws SQLException {
        try (Connection con = getConnection()) {
            return query(con, sql, params);
        }
    }

    public static TableData query(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int n = meta.getColumnCount();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                    columns.add(meta.getColumnLabel(i));
                }
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 1; i <= n; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    rows.add(row);
                }
                return new TableData(columns, rows);
            }
        }
    }

    public static Object scalar(String sql, Object... params) throws SQLException {
        try (Connection con = getConnection()) {
            return scalar(con, sql, params);
        }
    }

    public static Object scalar(Connection con, String sql, Object... params) throws SQLException {
        TableData t = query(con, sql, params);
        return t.isEmpty() ? null : t.rows().get(0)[0];
    }

    // ------------------------------------------------ INSERT / UPDATE / DELETE

    public static int update(String sql, Object... params) throws SQLException {
        try (Connection con = getConnection()) {
            return update(con, sql, params);
        }
    }

    public static int update(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------- Transaction

    @FunctionalInterface
    public interface SqlWork<T> {
        T run(Connection con) throws SQLException;
    }

    public static <T> T transaction(SqlWork<T> work) throws SQLException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                T result = work.run(con);
                con.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }


    public static String nextId(Connection con, String table, String column, String prefix) throws SQLException {
        Object max = scalar(con,
                "SELECT MAX(" + column + ") FROM " + table + " WHERE " + column + " LIKE ?",
                prefix + "%");
        int next = 1;
        if (max != null) {
            next = Integer.parseInt(max.toString().substring(prefix.length())) + 1;
        }
        return prefix + String.format("%03d", next);
    }
}
