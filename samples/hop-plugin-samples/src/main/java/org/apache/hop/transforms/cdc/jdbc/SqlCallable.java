package org.apache.hop.transforms.cdc.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlCallable<T> {
  T call() throws SQLException;
}
