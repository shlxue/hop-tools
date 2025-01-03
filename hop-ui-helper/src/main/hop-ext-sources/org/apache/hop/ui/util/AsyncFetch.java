package org.apache.hop.ui.util;

import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.eclipse.swt.widgets.Display;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class AsyncFetch {
  private static Logger logger = Logger.getLogger(AsyncFetch.class.getName());
  //  private static ILogChannel log = LogChannel.UI;
  private static final Queue<FutureHandler> futures = new ArrayBlockingQueue<>(1024);
  private static final ExecutorService executor = Executors.newCachedThreadPool();

  private final Display display;
  private final long timeoutMs;

  private AsyncFetch(Display display, long timeoutMs) {
    this.display = display;
    this.timeoutMs = timeoutMs;
  }

  public static AsyncFetch of(Display display) {
    return new AsyncFetch(display, 500);
  }

  //  public static <T> Future<Optional<T>> of(
  //    Callable<Optional<T>> callable, Consumer<T> ui, long timeoutMs) {
  //    Future<Optional<T>> submit = executor.submit(callable);
  //    return submit;
  //  }

  public final void tableFieldNames(
      IVariables variables,
      Supplier<DatabaseMeta> databaseMeta,
      String schema,
      String table,
      Consumer<String[]> fieldNamesConsumer) {
    tableFields(
        variables,
        databaseMeta,
        schema,
        table,
        rowMeta -> fieldNamesConsumer.accept(sortedFieldNames(rowMeta)));
  }

  public final void tableFields(
      IVariables variables,
      Supplier<DatabaseMeta> databaseMeta,
      String schema,
      String table,
      Consumer<IRowMeta> fieldsConsumer) {
    asyncCall(
        () -> {
          DatabaseMeta dm = databaseMeta.get();
          if (dm == null) {
            dm = new DatabaseMeta("local", "h2", "0", "localhost", "test", "9092", "", null);
          }
          try (Database database = new Database(null, variables, dm)) {
            database.connect();
            IRowMeta tableFieldsMeta = database.getTableFieldsMeta(schema, table);
            return tableFieldsMeta;
          } catch (HopDatabaseException e) {
            e.printStackTrace();
          } catch (Throwable e) {
            e.printStackTrace();
          }
          return new RowMeta();
        },
        5000,
        fieldsConsumer,
        err -> notifyError(table, err));
  }

  public final void prevTransformFieldNames(
      IVariables variables,
      PipelineMeta pipelineMeta,
      String transformName,
      Consumer<String[]> fieldNamesConsumer) {
    prevTransformFields(
        variables,
        pipelineMeta,
        transformName,
        rowMeta -> fieldNamesConsumer.accept(sortedFieldNames(rowMeta)));
  }

  public static String[] sortedFieldNames(IRowMeta rowMeta) {
    String[] fieldNames = rowMeta.getFieldNames();
    Arrays.sort(fieldNames);
    return fieldNames;
  }

  public final void prevTransformFields(
      IVariables variables,
      PipelineMeta pipelineMeta,
      String transformName,
      Consumer<IRowMeta> fieldsConsumer) {
    asyncCall(
        () -> pipelineMeta.getPrevTransformFields(variables, transformName),
        timeoutMs,
        fieldsConsumer,
        err -> notifyError(transformName, err));
  }

  private void notifyError(String source, Throwable error) {}

  private <T> Future<T> call(Callable<T> callable, Consumer<T> ui) {
    Future<T> submit =
        executor.submit(
            () -> {
              try {
                T optional = callable.call();
                display.asyncExec(() -> ui.accept(optional));
                return optional;
              } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
              }
            });
    return submit;
  }

  private <T> void asyncCall(
      Callable<T> callable, long timeoutMs, Consumer<T> onCompleted, Consumer<Throwable> onError) {
    long timeout = System.currentTimeMillis() + timeoutMs;
    Future<?> future =
        executor.submit(
            () -> {
              try {
                T result = callable.call();
                logger.config(
                    "Async fetch ui data: " + (result != null ? result.getClass().getName() : ""));
                display.asyncExec(() -> {
                  if (result != null) {
                    onCompleted.accept(result);
                  }
                });
              } catch (Throwable throwable) {
                display.asyncExec(() -> onError.accept(throwable));
                logger.warning("Error executing async call: " + throwable.getMessage());
              }
            });
    futures.offer(new FutureHandler(future, timeout));
  }

  private static class FutureHandler {
    private final Future<?> future;
    private final long timeout;

    public FutureHandler(Future<?> future, long timeout) {
      this.future = future;
      this.timeout = timeout;
    }
  }
}
