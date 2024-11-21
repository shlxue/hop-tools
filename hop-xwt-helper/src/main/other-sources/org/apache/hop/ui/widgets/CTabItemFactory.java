package org.apache.hop.ui.widgets;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;

public class CTabItemFactory extends AbstractItemFactory<CTabItemFactory, CTabItem, CTabFolder> {
  private CTabItemFactory(int style) {
    super(CTabItemFactory.class, cTabFolder -> new CTabItem(cTabFolder, style));
  }

  private CTabItemFactory(CTabItem control) {
    super(CTabItemFactory.class, cTabFolder -> control);
  }

  public static CTabItemFactory of(CTabItem control) {
    return new CTabItemFactory(control);
  }

  public static CTabItemFactory newTabItem(int style) {
    return new CTabItemFactory(style);
  }

  public CTabItemFactory control(Control control) {
    this.addProperty(cTabItem -> cTabItem.setControl(control));
    return this;
  }
}
