package org.apache.hop.ui.widgets;

import org.eclipse.swt.widgets.Control;

public interface ControlSupplier<W extends Control> {
  W get();
}
