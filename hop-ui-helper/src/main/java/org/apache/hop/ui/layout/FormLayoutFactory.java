package org.apache.hop.ui.layout;

import org.eclipse.swt.layout.FormLayout;

public class FormLayoutFactory extends BaseLayoutFactory<FormLayoutFactory, FormLayout> {
  FormLayoutFactory() {
    super(FormLayoutFactory.class, FormLayout::new);
  }

  public FormLayoutFactory margin(int top, int right, int bottom, int left) {
    return set(
        layout -> {
          layout.marginTop = top;
          layout.marginRight = right;
          layout.marginBottom = bottom;
          layout.marginLeft = left;
        });
  }

  public FormLayoutFactory margin(int width, int height) {
    return set(
        layout -> {
          layout.marginWidth = width;
          layout.marginHeight = height;
        });
  }

  public FormLayoutFactory spacing(int spacing) {
    return set(layout -> layout.spacing = spacing);
  }
}
