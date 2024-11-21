package org.apache.hop.testing.junit;

import org.apache.commons.io.file.PathUtils;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.testing.SqlMode;
import org.h2.server.Service;
import org.h2.tools.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public final class H2Server implements AutoCloseable {
  private static H2Server instance;

  private final Service service;
  private final AtomicInteger ref;
  private Path dataDir;

  public static H2Server getInstance(SqlMode sqlMode) {
    if (instance == null) {
      instance = new H2Server(getPort(sqlMode), sqlMode == SqlMode.POSTGRESQL);
    }
    return instance;
  }

  public H2Server(int port, boolean pgMode) {
    this.service = createH2Server(port, pgMode);
    this.ref = new AtomicInteger();
  }

  public String getUrl(SqlMode sqlMode) {
    if (sqlMode == SqlMode.H2) {
      return String.format("jdbc:h2:%s/test", service.getURL());
    }
    return String.format("jdbc:h2:%s/test;MODE=%s", service.getURL(), sqlMode);
  }

  public DatabaseMeta getDatabase() {
    String type = "H2";
    return new DatabaseMeta(
        "local", type, "0", "localhost", "test", Integer.toString(service.getPort()), "", "");
  }

  public Service getService() {
    synchronized (ref) {
      ref.incrementAndGet();
      return service;
    }
  }

  @Override
  public void close() {
    synchronized (ref) {
      if (ref.decrementAndGet() == 0) {
        service.stop();
        try {
          PathUtils.deleteDirectory(dataDir);
        } catch (IOException ignore) {
          // ignore
        } finally {
          instance = null;
        }
      }
    }
  }

  private Service createH2Server(int port, boolean pgMode) {
    try {
      dataDir = Files.createTempDirectory("junit.hop");
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create temporary directory: ", e);
    }
    // -tcpDaemon
    String args =
        String.format("-tcp -tcpDaemon -tcpPort %d -ifNotExists -baseDir %s", port, dataDir);
    String[] params = args.split(" ");
    try {
      if (pgMode) {
        return Server.createPgServer(params).start().getService();
      }
      return Server.createTcpServer(params).start().getService();
    } catch (SQLException e) {
      throw new IllegalStateException("Unable to create H2 server: " + args, e);
    }
  }

  private static int getPort(SqlMode sqlMode) {
    return switch (sqlMode) {
      case H2 -> 9092;
      case MYSQL, MARIADB -> 3306;
      case ORACLE -> 1521;
      case MSSQLSERVER -> 1433;
      case POSTGRESQL -> 3306;
      default -> 9092;
    };
  }
}
