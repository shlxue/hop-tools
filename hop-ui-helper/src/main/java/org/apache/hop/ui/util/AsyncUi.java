package org.apache.hop.ui.util;

import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class AsyncUi implements AutoCloseable {
  private final Display display;
  private final ExecutorService service;
  private final AtomicInteger reference;
  private CountDownLatch waitLatch;

  public AsyncUi(Display display, AtomicInteger reference) {
    this.display = display;
    this.service = Executors.newCachedThreadPool(new DefaultThreadFactory("UI"));
    this.reference = reference;
  }

  public CountDownLatch getWaitLatch() {
    if (waitLatch == null) {
      waitLatch = new CountDownLatch(reference.get());
    }
    return waitLatch;
  }

  @Override
  public void close() {
    if (reference.decrementAndGet() == 0) {
      service.shutdown();
    }
    waitLatch.countDown();
  }

  public <V> Optional<V> tryGet(Supplier<V> getter) {
    return runInUiThread(getter::get);
  }

  public <V> V get(Supplier<V> getter) {
    return runInUiThread(getter::get).orElseThrow();
  }

  public void set(Runnable runnable) {
    runInUiThread(runnable);
  }

  public void asyncExec(Runnable runnable) {
    service.execute(runnable);
  }

  public void runInUiThread(Runnable runnable) {
    runInUiThread(
        () -> {
          runnable.run();
          return null;
        });
  }

  public <V> Optional<V> runInUiThread(Callable<V> callable) {
    AtomicReference<V> ref = new AtomicReference<>();
    AtomicReference<Throwable> error = new AtomicReference<>();
    CountDownLatch waitFor = new CountDownLatch(1);
    display.asyncExec(() -> execCallable(callable, ref, error, waitFor));
    try {
      waitFor.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (error.get() != null) {
      Throwable e = error.get();
      if (error.get() instanceof RuntimeException ex) {
        throw ex;
      }
      throw new IllegalStateException(e);
    }
    return Optional.ofNullable(ref.get());
  }

  public void sleepMs(long timeout) {
    try {
      TimeUnit.MILLISECONDS.sleep(timeout);
    } catch (InterruptedException ignore) {
      Thread.currentThread().interrupt();
    }
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> T putDataIfAbsent(Widget widget, String key, Supplier<T> supplier) {
    Object value = widget.getData(key);
    if (value == null) {
      widget.setData(key, supplier.get());
    }
    return (T) widget.getData(key);
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> T removeData(Widget widget, String key) {
    Object value = widget.getData(key);
    if (value == null) {
      widget.setData(key, null);
    }
    return (T) value;
  }

  private <V> void execCallable(
      Callable<V> callable,
      AtomicReference<V> ref,
      AtomicReference<Throwable> error,
      CountDownLatch waitFor) {
    try {
      ref.set(callable.call());
    } catch (Throwable e) {
      error.set(e);
    } finally {
      waitFor.countDown();
    }
  }
}
