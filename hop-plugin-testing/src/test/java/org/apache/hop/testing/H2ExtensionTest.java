package org.apache.hop.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(H2Extension.class)
@HopEnv(withH2 = true)
class H2ExtensionTest {

  @Test
  void testH2() {
    assertEquals(3, queryRows(SqlMode.H2));
  }

  @Test
  void testMySql() {
    assertEquals(3, queryRows(SqlMode.MYSQL));
  }

  @Test
  void testOracle() {
    assertEquals(3, queryRows(SqlMode.ORACLE));
  }

  @Test
  void testPostgreSql() throws Exception {
    assertEquals(3, queryRows(SqlMode.POSTGRESQL));
  }

  private int queryRows(SqlMode sqlMode) {
    String jdbcUrl = "jdbc:h2:tcp://localhost:9092/test";
    if (sqlMode != SqlMode.H2) {
      jdbcUrl += ";MODE=" + sqlMode;
    }
    try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
      try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users")) {
        try (ResultSet rs = stmt.executeQuery()) {
          int count = 0;
          while (rs.next()) {
            count++;
          }
          return count;
        }
      }
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
