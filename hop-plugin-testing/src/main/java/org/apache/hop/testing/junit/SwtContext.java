package org.apache.hop.testing.junit;

import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.condition.OS;
import org.junit.platform.commons.util.Preconditions;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class SwtContext implements AutoCloseable {
  private static SwtContext instance;

  private final Display display;
  private final Shell shell;
  private final boolean headless;
  private final AtomicLong ref;

  public static synchronized SwtContext getInstance() {
    if (instance == null) {
      try {
        instance = new SwtContext();
      } catch (SWTException e) {
        if (OS.current() == OS.MAC && e.code == SWT.ERROR_THREAD_INVALID_ACCESS) {
          throw new IllegalStateException(
              "Check jvm option and junit parallel option: -XstartOnFirstThread & junit.jupiter.execution.parallel.enabled",
              e.getCause());
        }
        throw e;
      }
    }
    return instance;
  }

  private SwtContext() {
    this.display = Optional.ofNullable(Display.getDefault()).orElse(Display.getCurrent());
    this.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE | SWT.ON_TOP)
            .onClose(this::onCloseShell)
            .create(display);
    this.headless = GraphicsEnvironment.isHeadless();
    this.ref = new AtomicLong();
  }

  public Shell getShell() {
    synchronized (ref) {
      instance.ref.incrementAndGet();
      return shell;
    }
  }

  public boolean supportX11() {
    try {
      return GraphicsEnvironment.getLocalGraphicsEnvironment() != null;
    } catch (Exception ignore) {
      // ignore
    }
    return false;
  }

  public boolean isHeadless() {
    return headless;
  }

  public Shell newShell() {
    return Widgets.shell(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE)
        .layout(Layouts.from().get())
        .create(shell);
  }

  @Override
  public void close() {
    synchronized (ref) {
      if (ref.decrementAndGet() == 0) {
        shell.close();
        instance = null;
      }
    }
  }

  private void onCloseShell(ShellEvent event) {
    Preconditions.condition(
        ref.get() > 0, () -> "Abort closing shell, the current ref count " + ref.get());
  }
}
