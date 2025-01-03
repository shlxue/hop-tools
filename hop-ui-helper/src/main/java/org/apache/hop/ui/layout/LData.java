package org.apache.hop.ui.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.Optional;

public final class LData {
  private static final int MIDDLE = 35;

  private LData() {}

  //// FromData
  public static FormDataFactory form() {
    return new FormDataFactory(FormData::new);
  }

  public static FormDataFactory form(int width, int height) {
    return new FormDataFactory(() -> new FormData(width, height));
  }

  //// GridData
  public static GridDataFactory grid() {
    return new GridDataFactory(GridData::new);
  }

  public static GridDataFactory grid(int style) {
    return new GridDataFactory(() -> new GridData(style));
  }

  public static GridDataFactory grid(
      int hAlign, int vAlign, boolean hGrabSpace, boolean vGrabSpace) {
    return new GridDataFactory(() -> new GridData(hAlign, vAlign, hGrabSpace, vGrabSpace));
  }

  public static GridDataFactory grid(int hAlign, boolean hGrabSpace, int hSpan) {
    return grid(hAlign, GridData.CENTER, hGrabSpace, false, hSpan, 1);
  }

  public static GridDataFactory grid(int width, int height) {
    return new GridDataFactory(() -> new GridData(width, height));
  }

  public static GridDataFactory grid(
      int hAlign, int vAlign, boolean hGrabSpace, boolean vGrabSpace, int hSpan, int vSpan) {
    return new GridDataFactory(
        () -> new GridData(hAlign, vAlign, hGrabSpace, vGrabSpace, hSpan, vSpan));
  }

  //// RowData
  public static RowDataFactory row() {
    return new RowDataFactory(RowData::new);
  }

  public static RowDataFactory row(int width, int height) {
    return new RowDataFactory(() -> new RowData(width, height));
  }

  //// FromData for Dialog
  public static FormData byTop(Control top) {
    return byTop(top, true);
  }

  public static FormData byTop(Control top, int numerator) {
    return form().left(MIDDLE).top(top).right(numerator).get();
  }

  public static FormData byTop(Control top, double numerator) {
    return byTop(top, numerator, SWT.DEFAULT);
  }

  public static FormData byTop(Control top, double numerator, int hHint) {
    return form(SWT.DEFAULT, hHint)
        .left(MIDDLE)
        .top(top)
        .right(100)
        .bottom((int) (numerator * 100))
        .get();
  }

  public static FormData byTop(Control top, boolean fill) {
    return byTop(top, fill, SWT.DEFAULT, SWT.DEFAULT);
  }

  public static FormData byTop(Control top, boolean fill, int wHint) {
    return byTop(top, fill, wHint, SWT.DEFAULT);
  }

  public static FormData byTop(Control top, boolean fill, int wHint, int hHint) {
    if (top == null) {
      return onMiddle(fill, wHint, hHint).top(0).get();
    }
    return onMiddle(fill, wHint, hHint).top(top).get();
  }

  public static FormData byBottom(Control bottom) {
    return byBottom(bottom, true);
  }

  public static FormData byBottom(Control bottom, int numerator) {
    return form().left(MIDDLE).bottom(bottom).right(numerator).get();
  }

  public static FormData byBottom(Control bottom, double numerator) {
    return form().left(MIDDLE).bottom(bottom).right(100).top((int) (numerator * 100)).get();
  }

  public static FormData byBottom(Control bottom, boolean fill) {
    return byBottom(bottom, fill, SWT.DEFAULT, SWT.DEFAULT);
  }

  public static FormData byBottom(Control bottom, boolean fill, int wHint) {
    return byBottom(bottom, fill, wHint, SWT.DEFAULT);
  }

  public static FormData byBottom(Control bottom, boolean fill, int wHint, int hHint) {
    if (bottom == null) {
      return onMiddle(fill, wHint, hHint).bottom(100).get();
    }
    return onMiddle(fill, wHint, hHint).bottom(bottom).get();
  }

  public static FormData byRight(Control editor) {
    return byRight(editor, 0);
  }

  public static FormData byRight(Control editor, int offset) {
    return form().left(MIDDLE).top(editor, 0, SWT.CENTER).right(editor, offset).get();
  }

  public static FormData toLeft(Control top) {
    FormDataFactory factory = form().left(0);
    if (top != null) {
      factory.top(top);
    } else {
      factory.top(0);
    }
    return factory.get();
  }

  public static FormData toRight(Control top) {
    return toRight(top, 0);
  }

  public static FormData toRight(Control top, int offset) {
    return form().top(top).right(100, offset).get();
  }

  public static FormData fill(Control top, int hHint) {
    return form(SWT.DEFAULT, hHint).left(MIDDLE).top(top).right(100).get();
  }

  public static FormData fill(Control top, Control bottom, int hHint) {
    return fill(top, bottom, hHint, true);
  }

  public static FormData fill(Control top, Control bottom, int hHint, boolean hMiddle) {
    FormDataFactory factory = form(SWT.DEFAULT, hHint).left(hMiddle ? MIDDLE : 0).right(100);
    if (top != null) {
      factory.top(top);
    } else {
      factory.top(0);
    }
    if (bottom != null) {
      factory.bottom(bottom);
    } else {
      factory.bottom(100);
    }
    return factory.get();
  }

  public static FormData fill(Control top, Composite parent, int hHint) {
    return fill(top, parent, hHint, true);
  }

  public static FormData fill(Control top, Composite parent, int hHint, boolean hMiddle) {
    FormDataFactory factory = form(SWT.DEFAULT, hHint).left(hMiddle ? MIDDLE : 0).right(100);
    if (top != null) {
      factory.top(top);
    } else {
      factory.top(0);
    }
    Optional<Control> last = last(parent);
    if (last.isPresent()) {
      factory.bottom(last.get());
    } else {
      factory.bottom(100);
    }
    return factory.get();
  }

  public static FormData on(Control editor) {
    return form().top(editor, 0, SWT.CENTER).right(editor).get();
  }

  public static FormData on(Control editor, int wHint) {
    return form(wHint, SWT.DEFAULT).top(editor, 0, SWT.CENTER).right(editor).get();
  }

  public static FormData onTop(Control editor) {
    return form().top(editor, 0, SWT.TOP).right(editor).get();
  }

  private static FormDataFactory onMiddle(boolean fill, int wHint, int hHint) {
    if (fill) {
      return form(wHint, hHint).left(MIDDLE).right(100);
    }
    return form(wHint, hHint).left(MIDDLE);
  }

  private static Optional<Control> last(Composite parent) {
    if (parent != null) {
      Control[] children = parent.getChildren();
      return Optional.ofNullable(children.length > 0 ? children[children.length - 1] : null);
    }
    return Optional.empty();
  }
}
