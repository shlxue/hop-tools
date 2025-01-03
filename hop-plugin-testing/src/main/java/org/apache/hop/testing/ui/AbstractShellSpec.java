package org.apache.hop.testing.ui;

import org.apache.hop.ui.util.AsyncUi;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.Optional;

abstract class AbstractShellSpec<C extends Control> extends AbstractCompositeSpec<C, Shell> {

  @SuppressWarnings("unchecked")
  protected AbstractShellSpec(AsyncUi asyncUi) {
    this(asyncUi, (Class<C>) Control.class);
  }

  protected AbstractShellSpec(AsyncUi asyncUi, Class<C> childClass) {
    super(asyncUi, childClass);
  }

  protected Optional<Button> getDefaultButton() {
    return asyncUi.tryGet(shell::getDefaultButton);
  }

  protected boolean isOkButton(Control control) {
    return isButton(control, "&OK");
  }

  protected boolean isCancelButton(Control control) {
    return isButton(control, "&Cancel");
  }

  protected final <T extends Control> boolean isButton(T control, String text) {
    return control instanceof Button btn && btn.getText().contains(text);
  }

  protected boolean isHelpButton(Control control) {
    return control instanceof Button wHelp && wHelp.getText().contains("Help");
  }

  protected boolean isHopWidget(Composite control) {
    return control.getClass().getName().startsWith("org.apache.hop.ui");
  }
}
