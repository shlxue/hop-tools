package io.net;

import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.IntStream;

class OceanBaseTests {
//  static String url = "jdbc:oceanbase://192.168.1.52:2881/ROOT";
  static String url = "jdbc:oceanbase://192.168.1.52:2881/ch";
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
//    props.setProperty("user", "ROOT@oboracle");
//    props.setProperty("password", "Admin@123Admin@123");
    props.setProperty("user", "root@obmysql");
    props.setProperty("password", "Admin@123Admin@123");
    try (Connection connection = driver.connect(url, props)){
      try (PreparedStatement statement  = connection.prepareStatement("select * from aa")) {
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            System.out.println(111);
          }
          System.out.println("End");
        }
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      try (PreparedStatement statement  = connection.prepareStatement("DROP TABLE aa")) {
        statement.executeUpdate();
        connection.commit();
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      try (PreparedStatement statement  = connection.prepareStatement("create table aa(id int not null, name varchar(255) not null, updated_at date, primary key (id)); ")) {
        statement.executeUpdate();
        connection.commit();
        statement.executeUpdate("CREATE UNIQUE INDEX ix_name on aa (name);");
        connection.commit();
        statement.executeUpdate("CREATE INDEX ix_name_at on aa (updated_at);");
        connection.commit();
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      try (ResultSet rs = connection.getMetaData().getIndexInfo(null, null, "AA", false, false)) {
        while (rs.next()) {
          System.out.println(String.format("%s %s %s %s", rs.getObject(3), rs.getObject(6), rs.getObject(9), rs.getObject(7)));
        }
          System.out.println("End");
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      try (PreparedStatement statement  = connection.prepareStatement("select * from user_constraints where table_name='AA'")) {//where OBJECT_NAME like '%ind%' and OBJECT_TYPE ='TABLE'
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            System.out.println(String.format("%s %s", rs.getObject(1), rs.getObject(5)));
          }
          System.out.println("End");
        }
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      connection.setAutoCommit(false);
      try (PreparedStatement statement  = connection.prepareStatement("insert into aa(id, name) values(?,?)")) {
        System.out.println(statement.getParameterMetaData().getParameterCount());
        for(int i = 0; i < 10; i++) {
          statement.setInt(1, i+1);
          char n = (char) (((byte) 'a') + i);
          statement.setString(2, String.valueOf(n));
          statement.addBatch();
        }
        statement.executeBatch();
        connection.commit();
        statement.clearBatch();
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      try (PreparedStatement statement  = connection.prepareStatement("update aa set name = ? where id=?")) {
        for(int i = 0; i < 3; i++) {
          statement.setString(1, "a" + i);
          statement.setInt(2, i+1);
          statement.addBatch();
        }
        statement.setString(1, null);
        statement.setInt(2, 5);
        statement.addBatch();
        statement.setString(1, "f");
        statement.setInt(2, 4);
        statement.addBatch();
        statement.setString(1, null);
        statement.setInt(2, 6);
        statement.addBatch();
        for(int i = 6; i < 9; i++) {
          statement.setString(1, "a" + i);
          statement.setInt(2, i+1);
          statement.addBatch();
        }
        statement.executeBatch();
        connection.commit();
      } catch (BatchUpdateException e) {
        if (IntStream.of(e.getUpdateCounts()).anyMatch(updateCount -> updateCount > 0)) {
          connection.commit();
        } else {
          connection.rollback();
        }
      }  catch (SQLException e) {
        connection.rollback();
        System.err.println(e.getMessage());
//        e.printStackTrace();
      }
      try (PreparedStatement statement  = connection.prepareStatement("select * from aa")) {
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            System.out.println(String.format("%s %s", resultSet.getObject(1), resultSet.getObject(2)));
          }
          System.out.println("End");
        }
      }  catch (SQLException e) {
        e.printStackTrace();
      }
      try (PreparedStatement statement  = connection.prepareStatement("update aa set id = ? where name=?")) {
        statement.setInt(1, 1);
        statement.setString(2, "b");
        statement.executeUpdate();
        connection.commit();
      }  catch (SQLException e) {
//        e.printStackTrace();
        System.err.println(e.getMessage());
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    System.out.println();
  }

}
