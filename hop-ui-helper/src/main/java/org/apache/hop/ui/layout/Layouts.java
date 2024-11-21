package org.apache.hop.ui.layout;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;

public final class Layouts {
  private static final int DEFAULT_SPACING = 8;
  private static final int DEFAULT_MARGIN = 12;
  private static final int NESTED_MARGIN = 8;

  private Layouts() {}

  public static FillLayoutFactory fill() {
    return new FillLayoutFactory(FillLayout::new);
  }

  public static FillLayoutFactory fill(int type) {
    return new FillLayoutFactory(() -> new FillLayout(type));
  }

  public static FormLayoutFactory from() {
    return new FormLayoutFactory();
  }

  public static GridLayoutFactory grid() {
    return new GridLayoutFactory(GridLayout::new);
  }

  public static GridLayoutFactory grid(int numColumns) {
    return grid(numColumns, true);
  }

  public static GridLayoutFactory grid(int numColumns, boolean makeColumnsEqualWidth) {
    return new GridLayoutFactory(() -> new GridLayout(numColumns, makeColumnsEqualWidth));
  }

  public static RowLayoutFactory row() {
    return new RowLayoutFactory(RowLayout::new);
  }

  public static RowLayoutFactory row(int type) {
    return new RowLayoutFactory(() -> new RowLayout(type));
  }

  public static FormLayout defaultForm() {
    return from().spacing(DEFAULT_SPACING).margin(DEFAULT_MARGIN).get();
  }

  public static FormLayout nestedForm() {
    return from().spacing(DEFAULT_SPACING).margin(NESTED_MARGIN).get();
  }
}
