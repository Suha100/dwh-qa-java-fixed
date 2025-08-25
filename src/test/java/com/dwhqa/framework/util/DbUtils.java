package com.dwhqa.framework.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class DbUtils {
    private static Connection conn;
    public static Connection get() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection("jdbc:h2:mem:dwh;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
            }
            return conn;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
    public static void runSqlResource(String resourcePath) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (is == null) throw new RuntimeException("SQL resource not found: " + resourcePath);
            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            for (String stmt : sql.split(";\s*\n")) {
                String trimmed = stmt.trim();
                if (trimmed.isEmpty()) continue;
                try (Statement s = get().createStatement()) { s.execute(trimmed); }
            }
        } catch (Exception e) { throw new RuntimeException("Failed SQL " + resourcePath + ": " + e.getMessage(), e); }
    }
}
