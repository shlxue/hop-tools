package org.apache.hop.testing.ui;

import org.apache.hop.testing.SpecMode;
import org.apache.hop.testing.junit.AbstractSpecProvider;
import org.apache.hop.testing.junit.Spec;
import org.apache.hop.testing.tool.UiPreviewer;
import org.apache.hop.ui.util.AsyncUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Function;

class SwtWidgetSpecProvider<T extends Widget> extends AbstractSpecProvider<T, SpecMode, Shell> {
  protected final Display display;
  protected final AsyncUi asyncUi;

  SwtWidgetSpecProvider(
      Display display,
      AsyncUi asyncUi,
      Function<Shell, Spec<Shell, SpecMode, Shell>[]> lazyLoader) {
    super(SpecMode.class, lazyLoader);
    this.display = display;
    this.asyncUi = asyncUi;
  }

  @Override
  public void close() {
    dispatcher.dispose();
    asyncUi.close();
  }

  @Override
  protected Shell getDispatcher(T target) {
    return target.getDisplay().getActiveShell();
  }

  @Override
  protected boolean isDisposed(Shell target) {
    return target.isDisposed();
  }

  @Override
  protected Shell to(T target) {
    return target.getDisplay().getActiveShell();
  }

  @Override
  public void invoke(T target, SpecMode mode, Shell dispatcher) {
    super.invoke(target, mode, dispatcher);
    UiPreviewer.shellHanding(dispatcher);
  }
}
