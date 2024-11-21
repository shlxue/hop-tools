package org.apache.hop.ui.layout;

import org.eclipse.swt.layout.RowData;

import java.util.function.Supplier;

public class RowDataFactory extends AbstractLayoutDataFactory<RowDataFactory, RowData> {

  RowDataFactory(Supplier<RowData> creator) {
    super(RowDataFactory.class, creator.get());
  }

  public RowDataFactory exclude(boolean exclude) {
    return set(data -> data.exclude = exclude);
  }
}
