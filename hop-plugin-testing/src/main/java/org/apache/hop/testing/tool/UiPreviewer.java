package org.apache.hop.testing.tool;

import org.apache.hop.ui.util.SwtDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class UiPreviewer {
  private static final Logger logger = LoggerFactory.getLogger(UiPreviewer.class);

  static final String SWT_DEBUG_KEY = "SWT_DEBUG";
  private static UiPreviewer instance;
  private final Shell shell;
  private final boolean autoBlock;

  public static UiPreviewer get(Shell shell) {
    if (instance == null) {
      instance = new UiPreviewer(shell);
    } else if (!instance.shell.equals(shell)) {
      return new UiPreviewer(shell);
    }
    return instance;
  }

  private UiPreviewer(Shell shell) {
    this(shell, Boolean.getBoolean("SWT_AUTO_BLOCK"));
  }

  private UiPreviewer(Shell shell, boolean autoBlock) {
    this.shell = shell;
    this.autoBlock = autoBlock;
  }

  public <V extends Dialog> void show(Class<V> type) {
    show(type, false);
  }

  public <V extends Dialog> void show(Class<V> type, boolean block) {
    show(type, SWT.SHELL_TRIM, block);
  }

  public <V extends Dialog> void show(Class<V> type, int style) {
    show(type, style, false);
  }

  public <V extends Dialog> void show(Class<V> type, int style, boolean block) {
    show(() -> newDialog(type, style), block);
  }

  <V extends Dialog> void show(Supplier<V> creator) {
    show(creator, false);
  }

  <V extends Dialog> void show(Supplier<V> creator, boolean block) {
    if (shell == null || shell.isDisposed()) {
      return;
    }
    Shell[] children = shell.getShells();

    V dialog = creator.get();
    Shell loopShell = findEventLoopShell(shell, dialog, children);
    if (!loopShell.equals(shell)) {
      loopShell.setText(String.format("%s(%s)", loopShell.getText(), shell.getText()));
    }

    runEventLoop(loopShell.getDisplay(), loopShell);
  }

  public void preview(Shell shell) {
    preview(shell, false);
  }

  public void preview(Shell shell, boolean withColor) {
    //    Shell loopShell = findEventLoopShell()
    logger.info("...4");

    runEventLoop(shell.getDisplay(), shell, withColor);
  }

  void runEventLoop(Display display, Shell loopShell) {
    runEventLoop(display, loopShell, false);
  }

  public static void shellHanding(Shell shell) {
    runEventLoop(shell.getDisplay(), shell, false);
  }

  private static void runEventLoop(Display display, Shell loopShell, boolean withColor) {
    assert loopShell != null && !loopShell.isDisposed();
    if (isDebug(loopShell)) {
      logger.info("Active debug mode for shell: " + Integer.toHexString(loopShell.hashCode()));
      //      new SwtDebug(loopShell, withColor).debug(loopShell);
    }
    SwtDialog.runEventLoop(loopShell);
  }

  public static Shell findEventLoopShell(Shell parent, Dialog dialog, Shell[] before) {
    List<Shell> after = new ArrayList<>(Arrays.asList(dialog.getParent().getShells()));
    logger.info("Find the shell of dialog: {} -> {}", before.length, after.size());
    after.removeAll(Arrays.asList(before));
    if (after.size() > 1) {
      after.removeIf(item -> !parent.equals(item.getParent()));
      if (after.size() > 1) {
        Set<Shell> nestedShells = new HashSet<>();
        for (Shell shell : after) {
          nestedShells.addAll(Arrays.asList(shell.getShells()));
        }
        after.removeAll(nestedShells);
      }
    }
    return after.size() == 1 ? after.get(0) : parent;
  }

  private Shell findShellBySwtControl(Dialog dialog, Shell last) {
    for (Field field : dialog.getClass().getDeclaredFields()) {
      if (field.getType().isAssignableFrom(Control.class)) {
        ReflectionUtils.makeAccessible(field);
        //        try {
        //          ctl = (Control) ReflectionUtils.tryToReadFieldValue(field, dialog).get();
        //        } catch (Exception e) {
        //          logger.warning("Unable to access control field: " + field.getName());
        //          continue;
        //        }
        //        if (ctl.getShell().equals(last)) {
        //          return last;
        //        } else if (ctl.getShell().equals(last.getParent())) {
        //          return ctl.getShell();
        //        }
      }
    }
    return last;
  }

  private boolean isDialog(Constructor<?> creator) {
    Class<?>[] params = creator.getParameterTypes();
    if (params.length > 0 && params[0].equals(Shell.class)) {
      return params.length == 1 || params[1].equals(int.class);
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private <V extends Dialog> V newDialog(Class<V> type, int style) {
    List<Constructor<?>> constructors = ReflectionUtils.findConstructors(type, this::isDialog);
    try {
      if (!constructors.isEmpty()) {
        for (Constructor<?> constructor : constructors) {
          if (constructor.getParameterCount() == 1) {
            return (V) constructor.newInstance(shell);
          } else if (constructor.getParameterCount() == 2) {
            return (V) constructor.newInstance(shell, style);
          }
        }
      }
    } catch (ReflectiveOperationException ex) {
      throw new IllegalArgumentException("Create new dialog " + type, ex);
    }
    return ReflectionUtils.newInstance(type, true, new Class[] {Shell.class}, shell);
  }

  private static boolean isDebug(Shell shell) {
    String value = System.getProperty(SWT_DEBUG_KEY, System.getenv(SWT_DEBUG_KEY));
    if (Boolean.parseBoolean(value)) {
      return true;
    }
    while (shell != null) {
      if (shell.getData(SWT_DEBUG_KEY) != null) {
        return true;
      }
      shell = (Shell) shell.getParent();
    }
    return false;
  }
}
