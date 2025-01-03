package org.apache.hop.ui.widgets;

import org.eclipse.swt.events.*;

import java.util.function.Consumer;

public final class Adapter {
  private Adapter() {}

  // ControlListener
  public static ControlListener controlMoved(Consumer<ControlEvent> consumer) {
    return new ControlAdapter() {
      @Override
      public void controlMoved(ControlEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static ControlListener controlResized(Consumer<ControlEvent> consumer) {
    return new ControlAdapter() {
      @Override
      public void controlMoved(ControlEvent e) {
        consumer.accept(e);
      }
    };
  }

  // ExpandListener
  public static ExpandListener itemCollapsed(Consumer<ExpandEvent> consumer) {
    return new ExpandAdapter() {
      @Override
      public void itemCollapsed(ExpandEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static ExpandListener itemExpanded(Consumer<ExpandEvent> consumer) {
    return new ExpandAdapter() {
      @Override
      public void itemExpanded(ExpandEvent e) {
        consumer.accept(e);
      }
    };
  }

  // FocusListener
  public static FocusListener focusGained(Consumer<FocusEvent> c) {
    return new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        c.accept(e);
      }
    };
  }

  public static FocusListener focusLost(Consumer<FocusEvent> c) {
    return new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        c.accept(e);
      }
    };
  }

  // KeyListener
  public static KeyListener keyPressed(Consumer<KeyEvent> consumer) {
    return new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static KeyListener keyReleased(Consumer<KeyEvent> consumer) {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        consumer.accept(e);
      }
    };
  }

  // MenuListener
  public static MenuListener menuHidden(Consumer<MenuEvent> consumer) {
    return new MenuAdapter() {
      @Override
      public void menuHidden(MenuEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static MenuListener menuShown(Consumer<MenuEvent> consumer) {
    return new MenuAdapter() {
      @Override
      public void menuShown(MenuEvent e) {
        consumer.accept(e);
      }
    };
  }

  // MouseListener
  public static MouseListener mouseDoubleClick(Consumer<MouseEvent> consumer) {
    return new MouseAdapter() {
      @Override
      public void mouseDoubleClick(MouseEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static MouseListener mouseDown(Consumer<MouseEvent> consumer) {
    return new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static MouseListener mouseUp(Consumer<MouseEvent> consumer) {
    return new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        consumer.accept(e);
      }
    };
  }

  // MouseTrackListener
  public static MouseTrackListener mouseEnter(Consumer<MouseEvent> consumer) {
    return new MouseTrackAdapter() {
      @Override
      public void mouseEnter(MouseEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static MouseTrackListener mouseExit(Consumer<MouseEvent> consumer) {
    return new MouseTrackAdapter() {
      @Override
      public void mouseExit(MouseEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static MouseTrackListener mouseHover(Consumer<MouseEvent> consumer) {
    return new MouseTrackAdapter() {
      @Override
      public void mouseHover(MouseEvent e) {
        consumer.accept(e);
      }
    };
  }

  // SelectionListener
  public static SelectionListener widgetSelected(Consumer<SelectionEvent> consumer) {
    return new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        consumer.accept(e);
      }
    };
  }

  public static SelectionListener widgetDefaultSelected(Consumer<SelectionEvent> consumer) {
    return new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        consumer.accept(e);
      }
    };
  }

  // ShellListener
  public static ShellListener shellActivated(Consumer<ShellEvent> c) {
    return new ShellAdapter() {
      @Override
      public void shellActivated(ShellEvent e) {
        c.accept(e);
      }
    };
  }

  public static ShellListener shellClosed(Consumer<ShellEvent> c) {
    return new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        c.accept(e);
      }
    };
  }

  public static ShellListener shellDeactivated(Consumer<ShellEvent> c) {
    return new ShellAdapter() {
      @Override
      public void shellDeactivated(ShellEvent e) {
        c.accept(e);
      }
    };
  }

  public static ShellListener shellDeiconified(Consumer<ShellEvent> c) {
    return new ShellAdapter() {
      @SuppressWarnings("java:S1161")
      public void shellDeiconified(ShellEvent e) {
        c.accept(e);
      }
    };
  }

  public static ShellListener shellIconified(Consumer<ShellEvent> c) {
    return new ShellAdapter() {
      @SuppressWarnings("java:S1161")
      public void shellIconified(ShellEvent e) {
        c.accept(e);
      }
    };
  }

  // TreeListener
  public static TreeListener treeCollapsed(Consumer<TreeEvent> c) {
    return new TreeAdapter() {
      @Override
      public void treeCollapsed(TreeEvent e) {
        c.accept(e);
      }
    };
  }

  public static TreeListener treeExpanded(Consumer<TreeEvent> c) {
    return new TreeAdapter() {
      @Override
      public void treeExpanded(TreeEvent e) {
        c.accept(e);
      }
    };
  }
}
