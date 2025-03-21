package io.net;

import io.JdbcTest;
import org.junit.jupiter.api.Test;

import java.sql.*;

class PgIT extends JdbcTest<Statement> {

  PgIT() throws SQLException {
    super("postgresql");
  }

  @Test
  void name() throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("select 1; select 2;")) {
      while (stmt.getMoreResults()) {
        try (ResultSet rs = stmt.getResultSet()) {
          while (rs.next()) {
            System.out.printf("%s: %d\n", rs.getObject(1) );
          }
          System.out.println("end");
        }
      }
    }
  }
}
