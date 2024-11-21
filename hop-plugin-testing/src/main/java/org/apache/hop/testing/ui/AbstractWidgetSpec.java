package org.apache.hop.testing.ui;

import org.apache.hop.testing.SpecMode;
import org.apache.hop.testing.junit.Spec;
import org.apache.hop.ui.util.AsyncUi;
import org.apache.hop.ui.util.SwtDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AbstractWidgetSpec<T extends Widget> implements Spec<T, SpecMode, Shell>, AutoCloseable {

  protected Logger logger = LoggerFactory.getLogger(AbstractWidgetSpec.class);
  protected final AsyncUi asyncUi;
  protected Shell shell;
  protected T widget;
  protected SpecMode mode;

  public AbstractWidgetSpec(AsyncUi asyncUi) {
    this.asyncUi = asyncUi;
  }

  @Override
  public void invoke(T target, SpecMode mode, Shell dispatcher) {
    logger.trace("Apply {} ui-spec on {}", to(getClass()), to(target.getClass()));
    this.widget = target;
    this.mode = mode;
    this.shell = asyncUi.get(() -> getCurrentShell(dispatcher));
  }

  @Override
  public void close() {
    asyncUi.close();
  }

  protected int getStyle() {
    return asyncUi.get(shell::getStyle);
  }

  protected Point screenSize() {
    return asyncUi.get(() -> SwtDialog.screenSize(shell));
  }

  protected void delay() {
    delay(mode.getWaitTimeMs());
  }

  protected void delay(long waitingMs) {
    long waitingFor = System.currentTimeMillis() + waitingMs;
    long time = Math.min(10, waitingMs);
    while (waitingFor > System.currentTimeMillis()) {
      if (shell != null && shell.isDisposed()) {
        break;
      }
      asyncUi.sleepMs(time);
    }
  }

  protected final String to(Class<?> clazz) {
    return clazz.getSimpleName();
  }

  protected final String uiName() {
    if (shell == null) {
      return to(shell.getClass());
    }
    return "";
  }

  private Shell getCurrentShell(Shell parent) {
    if (parent.getShells().length > 0) {
      return parent.getShells()[0];
    }
    return null;
  }
}
