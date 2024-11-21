package org.apache.hop.ui.widgets;

import org.eclipse.swt.widgets.ToolBar;

public final class ToolBarFactory extends AbstractCompositeFactory<ToolBarFactory, ToolBar> {
  private ToolBarFactory(int style) {
    super(ToolBarFactory.class, parent -> new ToolBar(parent, style));
  }

  private ToolBarFactory(ToolBar control) {
    super(ToolBarFactory.class, parent -> control);
  }

  public static ToolBarFactory of(ToolBar control) {
    return new ToolBarFactory(control);
  }

  public static ToolBarFactory newToolBar(int style) {
    return new ToolBarFactory(style);
  }
}
