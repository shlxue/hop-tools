package org.apache.hop.testing.ui;

import org.apache.hop.ui.util.AsyncUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractControlSpec<T extends Control> extends AbstractWidgetSpec<T> {
  protected static final String DEBUG_TAB_KEY = "debug.tab";
  protected static final String DEBUG_POS_KEY = "debug.pos";

  public AbstractControlSpec(AsyncUi asyncUi) {
    super(asyncUi);
  }

  protected String getPosText(Control control) {
    List<String> list = new ArrayList<>();
    Control ref = control;
    while (ref != null) {
      Object val = ref.getData(DEBUG_POS_KEY);
      if (val == null) {
        break;
      }
      list.add(0, (String) val);
      ref = ref.getParent();
    }

    return String.join(".", list);
  }

  protected void onCustomPaint(Event event) {
    Control control = (Control) event.widget;
    String text = getPosText(control);
    if (text.isEmpty()) {
      return;
    }
    List<String> list = new ArrayList<>();
    Control ref = control;
    while (ref != null) {
      if (ref instanceof Shell) {
        break;
      }
      if (ref.getData(DEBUG_TAB_KEY) instanceof String) {
        list.add((String) ref.getData(DEBUG_TAB_KEY));
      }
      ref = ref.getParent();
    }
    Display display = Display.getDefault();
    //    if (ref != null) {
    //      display = ref.getDisplay();
    //    } else if (control.getParent() != null) {
    //      display = control.getParent().getDisplay();
    //    }
    Font font = new Font(display, "Arial", 10, SWT.NORMAL | SWT.INHERIT_DEFAULT);
    GC gc = new GC(display);
    boolean container = control instanceof Composite;
    gc.setForeground(display.getSystemColor(container ? SWT.COLOR_RED : SWT.COLOR_BLUE));
    gc.setFont(font);
    Point point = control.getSize();
    FontData fontData = font.getFontData()[0];
    int x = 0, y = 0;
    if (container) {
      //      y = fontData.getHeight();
    } else if (control instanceof Label) {
      y = point.y - fontData.getHeight();
      gc.setForeground(display.getSystemColor(SWT.COLOR_MAGENTA));
    } else {
      x = point.x - (text.length() * fontData.getHeight() / 2) - 3;
      y = point.y - fontData.getHeight();
    }
    gc.drawText(text, x, y, SWT.DRAW_TRANSPARENT);
    if ((!container || control instanceof Table)
        && !list.isEmpty()
        && !Label.class.equals(control.getClass())) {
      Collections.reverse(list);
      text = String.join(".", list);
      gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
      y = control instanceof Button ? 0 : point.y / 2 - fontData.getHeight();
      gc.drawText(text, point.x / 2, Math.max(y, 3), SWT.DRAW_TRANSPARENT);
    }
    gc.dispose();
    font.dispose();
  }
}
