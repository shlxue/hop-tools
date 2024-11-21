package org.apache.hop.testing;

public enum SqlMode {
  DB2("DB2"),
  DERBY("DERBY"),
  H2("H2"),
  HSQLDB("HSQLDB"),
  MSSQLSERVER("MSSQLServer"),
  MARIADB("MariaDB"),
  MYSQL("MySQL"),
  ORACLE("Oracle"),
  POSTGRESQL("PostgreSQL");

  private final String mode;

  SqlMode(String mode) {
    this.mode = mode;
  }

  public String getMode() {
    return mode;
  }
}
