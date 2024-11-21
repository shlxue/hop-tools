package org.apache.hop.testing.junit;

import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.platform.commons.util.Preconditions;

import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;

public final class SwtContext implements AutoCloseable {
  private static SwtContext instance;

  private final Shell shell;
  private final boolean headless;
  private final AtomicLong ref;

  public static synchronized SwtContext getInstance() {
    if (instance == null) {
      try {
        instance = new SwtContext();
      } catch (Exception ignore) {
        // ignore
        ignore.printStackTrace();
      }
    }
    return instance;
  }

  private SwtContext() {
    this.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE | SWT.ON_TOP)
            .onClose(this::onCloseShell)
            .create((Composite) null);
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
