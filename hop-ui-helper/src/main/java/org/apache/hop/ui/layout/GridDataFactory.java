package org.apache.hop.ui.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

import java.util.function.Supplier;

public class GridDataFactory extends AbstractLayoutDataFactory<GridDataFactory, GridData> {
  GridDataFactory(Supplier<GridData> creator) {
    super(GridDataFactory.class, creator.get());
  }

  public GridDataFactory span(int hSpan) {
    return span(hSpan, -1);
  }

  public GridDataFactory span(int hSpan, int vSpan) {
    return set(
        gridData -> {
          if (hSpan > 0) {
            gridData.horizontalSpan = hSpan;
          }
          if (vSpan > 0) {
            gridData.verticalSpan = vSpan;
          }
        });
  }

  public GridDataFactory hint(int width) {
    return hint(width, SWT.DEFAULT);
  }

  public GridDataFactory hint(int width, int height) {
    return set(
        gridData -> {
          gridData.widthHint = width;
          gridData.heightHint = height;
        });
  }
}
