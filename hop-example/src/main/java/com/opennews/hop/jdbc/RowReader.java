package com.opennews.hop.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowReader<V> {
  V read(ResultSet rs) throws SQLException;
}
