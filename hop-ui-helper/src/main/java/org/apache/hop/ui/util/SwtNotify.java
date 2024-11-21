package org.apache.hop.ui.util;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

public final class SwtNotify {
  private SwtNotify() {}

  public static void notify(Widget target, int eventType) {
    Event event = new Event();
    event.widget = target;
    target.notifyListeners(eventType, event);
  }
}
