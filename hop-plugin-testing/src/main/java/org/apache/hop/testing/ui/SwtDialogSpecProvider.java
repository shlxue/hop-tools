package org.apache.hop.testing.ui;

import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.testing.junit.AbstractSpecProvider;
import org.apache.hop.testing.junit.HopUiHelper;
import org.apache.hop.testing.junit.Spec;
import org.apache.hop.ui.util.AsyncUi;
import org.apache.hop.ui.util.SwtDialog;
import org.apache.hop.workflow.action.IActionDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

class SwtDialogSpecProvider<T extends Dialog> extends AbstractSpecProvider<T, SpecMode, Shell> {
  protected final Display display;
  protected final AsyncUi asyncUi;
  private final AtomicReference<Shell> subShell = new AtomicReference<>();
  private final boolean usingOpenMode;

  SwtDialogSpecProvider(
      Display display,
      AsyncUi asyncUi,
      Function<Shell, Spec<Shell, SpecMode, Shell>[]> lazyLoader) {
    this(display, asyncUi, false, lazyLoader);
  }

  SwtDialogSpecProvider(
      Display display,
      AsyncUi asyncUi,
      boolean usingOpenMode,
      Function<Shell, Spec<Shell, SpecMode, Shell>[]> lazyLoader) {
    super(SpecMode.class, lazyLoader);
    this.display = display;
    this.asyncUi = asyncUi;
    this.usingOpenMode = usingOpenMode;
    asyncUi.getWaitLatch();
  }

  @Override
  protected Shell getDispatcher(Dialog target) {
    return target.getParent();
  }

  @Override
  protected void onPostTest() {
    try {
      Shell shell = subShell.get();
      while (true) {
        if (asyncUi.getWaitLatch().await(500, TimeUnit.MILLISECONDS)) {
          break;
        }
        if (shell == null || shell.isDisposed()) {
          break;
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      if (subShell.get() != null) {
        subShell.get().dispose();
      }
    }
  }

  @Override
  protected Shell to(T target) {
    return subShell.get();
  }

  @Override
  public void close() {
    super.close();
    if (subShell.get() != null) {
      subShell.get().dispose();
    }
    asyncUi.close();
    //    if (asyncUi.getWaitLatch().getCount() == 0) {
    //      dispatcher.dispose();
    //    }
  }

  @Override
  protected boolean isDisposed(Shell target) {
    int c = target.getShells().length;
    return target.isDisposed();
  }

  @Override
  public void invoke(T target, SpecMode mode, Shell dispatcher) {
    super.invoke(target, mode, dispatcher);
    Shell[] shells = dispatcher.getDisplay().getShells();
    System.out.println(shells.length);
    if (usingOpenMode
        || HopUiHelper.isPluginUi(target.getClass()) && !isConstructorBuilder(dispatcher)) {
      legacyShellHanding(target, mode.isColorized());
      return;
    }
    subShell.set(dispatcher.getShells()[0]);
    int count = dispatcher.getShells().length;
    onActiveShell(subShell.get(), mode.isColorized());
    asyncUi.asyncExec(() -> onActionDialog(count, null));
    SwtDialog.runEventLoop(subShell.get());
  }

  private boolean isConstructorBuilder(Shell shell) {
    return shell.getShells().length > 0;
  }

  private void legacyShellHanding(T dialog, boolean visible) {
    int count = dispatcher.getShells().length;
    asyncUi.asyncExec(() -> onActionDialog(count, visible));
    Callable<?> open = null;
    if (dialog instanceof ITransformDialog transformDialog) {
      open = transformDialog::open;
    } else if (dialog instanceof IActionDialog actionDialog) {
      open = actionDialog::open;
    }
    if (open != null) {
      Callable<?> activeUi = open;
      try {
        open.call();
      } catch (Throwable e) {
        Assertions.fail("Active plugin ui", e);
      }
      //      asyncUi.asyncExec(() -> asyncUi.runInUiThread(activeUi));
      // asyncUi.asyncExec(() -> onActionDialog(count, visible));
    } else {
      Assertions.fail("Invalid hop plugin ui by dialog " + dialog.getClass());
    }
  }

  private void onActionDialog(int count, Boolean visible) {
    if (count == 0) {
      do {
        asyncUi.sleepMs(1);
      } while (asyncUi.get(() -> dispatcher.getShells().length) <= count);
    }
    if (subShell.get() == null) {
      asyncUi.runInUiThread(() -> subShell.set(dispatcher.getShells()[0]));
      while (subShell.get() == null) {
        asyncUi.sleepMs(1);
      }
    }
    if (usingOpenMode) {
      Shell shell = subShell.get();
      asyncUi.runInUiThread(shell::setActive);
      System.out.println(Integer.toHexString(shell.hashCode()));
      while (!asyncUi.runInUiThread(() -> isVisible(shell)).orElse(false)) {
        asyncUi.sleepMs(2);
      }
    }
    //    beforeActionShell.accept(subShell.get());
    if (visible != null) {
      asyncUi.runInUiThread(() -> onActiveShell(subShell.get(), visible));
    }
    onShellAction(null);
  }

  private boolean isVisible(Shell shell) {
    return shell.isVisible() || Arrays.stream(shell.getChildren()).anyMatch(Control::isVisible);
  }

  private void onActiveShell(Shell shell, Boolean visible) {
    if (Boolean.TRUE.equals(visible)) {
      adjustSize(shell, shell.getSize(), SwtDialog.screenSize(shell), screenFix(shell));
    } else if (visible != null) {
      //      shell.setVisible(false);
    }
  }

  private void adjustSize(Shell shell, Point size, Point screenArea, Point screenFix) {
    Point central = new Point(screenArea.x / 2, screenArea.y / 2);
    Point half = new Point(size.x / 2, size.y / 2);
    //    shell.setLocation(central.x - half.x + screenFix.x, central.y - half.y + screenFix.y);
    //    shell.setSize(size);
    //    shell.setBounds(
    //        central.x - half.x + screenFix.x, central.y - half.y + screenFix.y, size.x, size.y);
  }

  private Point screenFix(Shell shell) {
    Rectangle clientArea = shell.getMonitor().getClientArea();
    return new Point(clientArea.x, clientArea.y);
  }
}
