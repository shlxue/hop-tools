package io.net;

import java.sql.*;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class PostgresqlTest {
  static String url = "jdbc:postgresql://192.168.1.16:5432/postgres";
  static Driver driver;

  static {
    try {
      driver = DriverManager.getDriver(url);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void name() {
    Properties props = new Properties();
    props.setProperty("user", "cbd_admin");
    props.setProperty("password", "45rtfgvbNHY^");
    try (Connection connection = driver.connect(url, props)){
      try (PreparedStatement statement  = connection.prepareStatement("select * from xx")) {
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            System.out.println(111);
          }
          System.out.println("End");
        }
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      try (PreparedStatement statement  = connection.prepareStatement("select now()")) {
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            System.out.println(resultSet.getObject(1));
          }
          System.out.println("End");
        }
      }  catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    System.out.println();
  }
}
