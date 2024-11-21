package org.apache.hop.ui.util;

import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.variables.IVariables;

import java.sql.SQLException;
import java.util.function.Consumer;

public class JdbcHelper {
  private final ILogChannel log = LogChannel.GENERAL;
  private final ILoggingObject parent;
  private final IVariables variables;
  private final DatabaseMeta databaseMeta;

  public static JdbcHelper of(ILoggingObject parent, IVariables variables, DatabaseMeta dbMeta) {
    return new JdbcHelper(parent, variables, dbMeta);
  }

  private JdbcHelper(ILoggingObject parent, IVariables variables, DatabaseMeta databaseMeta) {
    this.parent = parent;
    this.variables = variables;
    this.databaseMeta = databaseMeta;
  }

  void query(JdbcCall<Database> queryCall, Consumer<Throwable> onError) {
    try (Database database = new Database(parent, variables, databaseMeta)) {
      database.connect();
      queryCall.exec(database);
    } catch (Throwable e) {
      log.logError("Error connecting to the database", e);
      onError.accept(e);
    }
  }

  interface JdbcCall<T> {
    void exec(T database) throws SQLException, HopDatabaseException;
  }
}
