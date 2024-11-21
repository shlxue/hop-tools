package org.apache.hop.ui.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import java.util.ArrayList;
import java.util.List;

final class Monitors {

  static final Point[] MONITOR_SIZES;
  static final Point[] SCREEN_SIZES;
  static final Point PRIMARY_SCREEN_SIZE;
  static final Point DEFAULT_SCREEN_SIZE;
  static final Point[] DIALOG_SIZES;

  static {
    Monitor[] monitors = Display.getDefault().getMonitors();
    MONITOR_SIZES = new Point[monitors.length];
    SCREEN_SIZES = new Point[monitors.length];
    for (int i = 0; i < monitors.length; i++) {
      MONITOR_SIZES[i] = to(monitors[i].getBounds());
      SCREEN_SIZES[i] = to(monitors[i].getClientArea());
    }
    Monitor primaryMonitor = Display.getDefault().getPrimaryMonitor();
    PRIMARY_SCREEN_SIZE = to(primaryMonitor.getClientArea());
    DEFAULT_SCREEN_SIZE = to(primaryMonitor.getClientArea());
    DIALOG_SIZES =
        new Point[] {
          new Point(480, 272),
          new Point(640, 350),
          new Point(800, 480),
          new Point(960, 540),
          new Point(1024, 768),
          new Point(1280, 800),
          new Point(1440, 900),
          new Point(1600, 1080),
          new Point(1920, 1080),
          new Point(2560, 1440),
          new Point(3840, 2160),
        };
  }

  private Monitors() {}

  static Point reset(Point size) {
    if (size.x != DEFAULT_SCREEN_SIZE.x || size.y != DEFAULT_SCREEN_SIZE.y) {
      DEFAULT_SCREEN_SIZE.x = size.x;
      DEFAULT_SCREEN_SIZE.y = size.y;
    }
    return DEFAULT_SCREEN_SIZE;
  }

  static Point getClientAreaSize(Monitor monitor) {
    return to(monitor.getClientArea());
  }

  static Point preferredSize(Point hintSize) {
    for (Point size : DIALOG_SIZES) {
      if (size.x < hintSize.x || size.y < hintSize.y) {
        continue;
      }
      return size;
    }
    return DEFAULT_SCREEN_SIZE;
  }

  static Point[] preferredSizes(Point hintSize) {
    return preferredSizes(hintSize, DEFAULT_SCREEN_SIZE);
  }

  static Point[] preferredSizes(Point minSize, Point maxSize) {
    List<Point> list = new ArrayList<>(DIALOG_SIZES.length);
    for (Point size : DIALOG_SIZES) {
      if (minSize.x > size.x || minSize.y > size.y || maxSize.x < size.x || maxSize.y < size.y) {
        continue;
      }
      list.add(size);
    }
    if (!list.isEmpty()) {
      Point last = list.get(list.size() - 1);
      if (maxSize.x > last.x || maxSize.y > last.y) {
        list.add(maxSize);
      }
    }
    return list.toArray(new Point[0]);
  }

  private static Point to(Rectangle rectangle) {
    return new Point(rectangle.width, rectangle.height);
  }
}
