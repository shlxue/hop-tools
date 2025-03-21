package io;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.function.Consumer;

public abstract class JdbcTest<T> {
  protected static final Properties cfg = new Properties();
  protected static final Properties config = new Properties();
  private final Properties info;
  protected Connection conn;
  protected T target;

  static {
    try (InputStream is = JdbcTest.class.getResourceAsStream("/jdbc-dialect.properties")) {
      cfg.load(is);
    } catch (IOException ignored) {
    }
    try (InputStream is = JdbcTest.class.getResourceAsStream("/jdbc.properties")) {
      config.load(is);
    } catch (IOException ignored) {
    }
  }

  public JdbcTest(String type) throws SQLException {
    this.info = new Properties();
    info.put("user", get(type, "user"));
    info.put("password", get(type, "password"));
    this.conn = DriverManager.getConnection(get(type, "url"), info);
  }

  private static String get(String type, String key) {
    return config.getProperty(key(type, key));
  }

  private static String key(String type, String key) {
    return String.format("jdbc.%s.%s", type, key);
  }

  protected int bulkInsert(Statement statement) throws SQLException {
    return 0;
  }

  protected int fetch(String sql, Consumer<ResultSet> reader) throws SQLException {
    int count = 0;
    try (Statement stmt = conn.prepareStatement(sql)) {
      try (ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
          count++;
          reader.accept(rs);
        }
      }
    }
    return count;
  }


  protected int[] batch(PreparedStatement statement) throws SQLException {
    return new int[0];
  }
}
