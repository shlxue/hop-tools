package io.net;

import io.JdbcTest;

import java.sql.SQLException;

class PgIT extends JdbcTest {

  public PgIT() throws SQLException {
    super("postgresql");
  }

}
