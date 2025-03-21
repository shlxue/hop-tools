package io.net;

import io.JdbcTest;
import org.junit.jupiter.api.Test;

import java.sql.*;

class MysqlIT extends JdbcTest<Statement> {

  MysqlIT() throws SQLException {
    super("mysql");
  }

  @Test
  void name() throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("select table_name from information_schema.tables", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
      try (ResultSet rs = stmt.executeQuery()) {
        stmt.setFetchSize(100);
        while (rs.next()) {
          System.out.printf("%s\n", rs.getObject(1));
        }
        System.out.println("end");
      }
      while (stmt.getMoreResults()) {
        try (ResultSet rs = stmt.getResultSet()) {
          while (rs.next()) {
            System.out.printf("%s: %d\n", rs.getObject(1));
          }
          System.out.println("end");
        }
      }
    }
  }
}
