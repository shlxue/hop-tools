package com.opennews.hop.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlCallable<T> {
  T call() throws SQLException;
}
