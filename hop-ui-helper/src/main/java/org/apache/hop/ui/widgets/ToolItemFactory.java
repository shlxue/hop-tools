package org.apache.hop.ui.widgets;

import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public final class ToolItemFactory extends AbstractItemFactory<ToolItemFactory, ToolItem, ToolBar> {
  private ToolItemFactory(int style) {
    super(ToolItemFactory.class, toolBar -> new ToolItem(toolBar, style));
  }

  public static ToolItemFactory newToolItem(int style) {
    return new ToolItemFactory(style);
  }

  private ToolItemFactory(ToolItem control) {
    super(ToolItemFactory.class, toolBar -> control);
  }

  public static ToolItemFactory of(ToolItem control) {
    return new ToolItemFactory(control);
  }
}
