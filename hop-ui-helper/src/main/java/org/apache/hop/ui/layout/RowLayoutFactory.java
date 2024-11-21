package org.apache.hop.ui.layout;

import org.eclipse.swt.layout.RowLayout;

import java.util.function.Supplier;

public class RowLayoutFactory extends BaseLayoutFactory<RowLayoutFactory, RowLayout> {

  RowLayoutFactory(Supplier<RowLayout> creator) {
    super(RowLayoutFactory.class, creator);
  }

  @Override
  public RowLayoutFactory spacing(int spacing) {
    return set(layout -> layout.spacing = spacing);
  }

  public RowLayoutFactory margin(int width, int height) {
    return set(
        layout -> {
          layout.marginWidth = width;
          layout.marginHeight = height;
        });
  }

  @Override
  public RowLayoutFactory margin(int top, int right, int bottom, int left) {
    return set(
        layout -> {
          layout.marginLeft = left;
          layout.marginTop = top;
          layout.marginRight = right;
          layout.marginBottom = bottom;
        });
  }
}
