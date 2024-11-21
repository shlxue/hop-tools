package org.apache.hop.ui.layout;

import org.eclipse.swt.layout.GridLayout;

import java.util.function.Supplier;

public class GridLayoutFactory extends BaseLayoutFactory<GridLayoutFactory, GridLayout> {
  GridLayoutFactory(Supplier<GridLayout> creator) {
    super(GridLayoutFactory.class, creator);
  }

  public GridLayoutFactory clean(boolean h) {
    return set(
        layout -> {
          if (h) {
            layout.marginWidth = 0;
          } else {
            layout.marginHeight = 0;
          }
        });
  }

  public GridLayoutFactory clean(boolean h, boolean v) {
    return set(
        layout -> {
          if (h) {
            layout.marginWidth = 0;
          }
          if (v) {
            layout.marginHeight = 0;
          }
        });
  }

  public GridLayoutFactory margin(int width, int height) {
    return set(
        layout -> {
          layout.marginWidth = width;
          layout.marginHeight = height;
        });
  }

  public GridLayoutFactory margin(int top, int right, int bottom, int left) {
    return set(
        layout -> {
          layout.marginTop = top;
          layout.marginRight = right;
          layout.marginBottom = bottom;
          layout.marginLeft = left;
        });
  }

  public GridLayoutFactory spacing(int spacing) {
    return spacing(spacing, spacing);
  }

  public GridLayoutFactory spacing(int horizontal, int vertical) {
    return set(
        layout -> {
          layout.horizontalSpacing = horizontal;
          layout.verticalSpacing = vertical;
        });
  }
}
