package org.apache.hop.ui.layout;

import org.eclipse.swt.layout.FillLayout;

import java.util.function.Supplier;

public class FillLayoutFactory extends AbstractLayoutFactory<FillLayoutFactory, FillLayout> {

  FillLayoutFactory(Supplier<FillLayout> creator) {
    super(FillLayoutFactory.class, creator);
  }

  public FillLayoutFactory margin(int width, int height) {
    return set(
        fillLayout -> {
          fillLayout.marginWidth = width;
          fillLayout.marginHeight = height;
        });
  }

  public FillLayoutFactory spacing(int spacing) {
    return set(fillLayout -> fillLayout.spacing = spacing);
  }
}
