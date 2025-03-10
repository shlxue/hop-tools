package org.apache.hop.testing;

import org.apache.hop.testing.junit.H2Server;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class H2Extension implements BeforeAllCallback, AfterAllCallback {
  private static final Logger logger = LoggerFactory.getLogger(H2Extension.class);

  @Override
  public void beforeAll(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    HopEnv hopEnv = testClass.getAnnotation(HopEnv.class);
    Preconditions.condition(StatusUtil.withH2(hopEnv), "withH2 is not enabled");
    try {
      List<String> scripts = searchScripts(testClass.getClassLoader());
      H2Server server =
          StatusUtil.get(
              context,
              StoreKey.HOP_H2_SERVER,
              H2Server.class,
              key -> H2Server.getInstance(StatusUtil.sqlMode(hopEnv)));
      logger.debug("Started H2 server: " + server.getService().getURL());
      if (!scripts.isEmpty()) {
        initSchemas(server.getUrl(hopEnv.sqlMode()), scripts);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Search sql scripts", e);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    H2Server server = StatusUtil.get(context, StoreKey.HOP_H2_SERVER, H2Server.class);
    if (server != null) {
      logger.debug("Close h2 server...");
      server.close();
      StatusUtil.remove(context, StoreKey.HOP_H2_SERVER);
    }
  }

  private List<String> searchScripts(ClassLoader classLoader) throws IOException {
    List<String> list = new ArrayList<>();
    for (String path : new String[] {"schema.sql", "data.sql"}) {
      try (InputStream is = classLoader.getResourceAsStream(path)) {
        logger.trace("Loading init sql script: {}" + path);
        if (is != null) {
          list.add(new String(is.readAllBytes()));
        }
      }
    }
    return list;
  }

  private void initSchemas(String jdbcUrl, List<String> scripts) {
    AtomicInteger count = new AtomicInteger();
    String sqlSpector = ";\\n";
    try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
      for (String script : scripts) {
        for (String sql : script.split(sqlSpector)) {
          if (StringUtils.isNotBlank(script)) {
            runScript(conn, sql.trim());
            count.incrementAndGet();
          }
        }
      }
      if (count.get() > 0) {
        logger.trace("Init sql scripts: " + count.get());
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Execute sql script failed on h2", e);
    }
  }

  private void runScript(Connection conn, String sql) throws SQLException {
    String prefix = sql.substring(0, Math.min(sql.length(), 32)).replace('\n', ' ');
    logger.trace(
        String.format(
            "Preparing sql script(%d): %s...", sql.length(), prefix.replaceAll("\\s{2,}", " ")));
    String shortSql = sql.replaceAll("\\s+", " ");
    try (PreparedStatement ps = conn.prepareStatement(shortSql)) {
      ps.executeUpdate();
    } finally {
      if (!conn.getAutoCommit()) {
        conn.commit();
      }
    }
  }
}
