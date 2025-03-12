package io;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class MetaDataTest extends JdbcTest<DatabaseMetaData> {
  public MetaDataTest(String type) throws SQLException {
    super(type);
    this.target = conn.getMetaData();
  }
}
