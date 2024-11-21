package org.apache.hop.ui.widgets;

import org.eclipse.swt.widgets.Control;

public interface VarSupplier<V extends Control> {
  V get();
}
