package org.apache.hop.testing.ui;

import org.apache.hop.testing.SpecMode;
import org.apache.hop.testing.junit.Spec;
import org.apache.hop.testing.ui.ShellSpec.CheckAutoLayout;
import org.apache.hop.testing.ui.ShellSpec.PreviewAutoLayout;
import org.apache.hop.testing.ui.ShellSpec.Tags;
import org.apache.hop.ui.util.AsyncUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.InvocationInterceptor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class UiSpecs<W> {
  static final int[] SWT_COLORS =
      new int[] {
        SWT.COLOR_RED,
        SWT.COLOR_GREEN,
        SWT.COLOR_YELLOW,
        SWT.COLOR_BLUE,
        SWT.COLOR_MAGENTA,
        SWT.COLOR_CYAN,
        SWT.COLOR_GRAY,
      };
  static final int[] SWT_DARK_COLORS =
      new int[] {
        SWT.COLOR_DARK_RED,
        SWT.COLOR_DARK_GREEN,
        SWT.COLOR_DARK_YELLOW,
        SWT.COLOR_DARK_BLUE,
        SWT.COLOR_DARK_MAGENTA,
        SWT.COLOR_DARK_CYAN,
        SWT.COLOR_DARK_GRAY,
      };

  private final Shell parent;
  private final Class<?> paramType;
  private final Set<Spec<W, SpecMode, Shell>> specs;
  private AtomicInteger refCount;
  private AsyncUi asyncUi;

  private UiSpecs(Shell parent, Class<W> paramType) {
    this.parent = parent;
    this.paramType = paramType;
    this.specs = new LinkedHashSet<>();
    this.refCount = new AtomicInteger();
    this.asyncUi = new AsyncUi(parent.getDisplay(), refCount);
  }

  public static <T> UiSpecs<T> builder(Shell parent, Class<T> paramType) {
    return new UiSpecs<>(parent, paramType);
  }

  public InvocationInterceptor build() {
    return build(true);
  }

  @SuppressWarnings("unchecked")
  private boolean isCloseSpec(Spec spec) {
    return spec instanceof ShellSpec.DelayClose || specs instanceof ShellSpec.AutoClose;
  }

  @SuppressWarnings("unchecked")
  public InvocationInterceptor build(boolean clean) {
    if (specs.stream().noneMatch(this::isCloseSpec)) {
      add(new ShellSpec.AutoClose(asyncUi));
    }
    try {
      List<Spec> uiSpecs = new ArrayList<>(specs);
      if (Dialog.class.isAssignableFrom(paramType)) {
        return new SwtDialogSpecProvider<>(
            parent.getDisplay(), asyncUi, shell -> uiSpecs.toArray(Spec[]::new));
      }
      return new SwtWidgetSpecProvider<>(
          parent.getDisplay(), asyncUi, shell -> uiSpecs.toArray(Spec[]::new));
    } finally {
      if (clean) {
        specs.clear();
        refCount = new AtomicInteger();
        asyncUi = new AsyncUi(parent.getDisplay(), refCount);
      }
    }
  }

  public UiSpecs<W> previewAutoLayout() {
    return add(new PreviewAutoLayout(asyncUi));
  }

  public UiSpecs<W> buildUi() {
    return add(new ShellSpec.UiBuilder(asyncUi));
  }

  public UiSpecs<W> defaultButton() {
    return add(new ShellSpec.DefaultButton(asyncUi));
  }

  public UiSpecs<W> lazyApplyTheme() {
    return add(new ShellSpec.LazyApplyTheme(asyncUi));
  }

  public UiSpecs<W> okListener() {
    return add(new ShellSpec.OkListener(asyncUi));
  }

  public UiSpecs<W> minimum() {
    return add(new ShellSpec.Minimum(asyncUi));
  }

  public UiSpecs<W> checkAutoLayout() {
    return add(new CheckAutoLayout(asyncUi));
  }

  public UiSpecs<W> switchFocus() {
    return add(new ShellSpec.SwitchFocus(asyncUi));
  }

  public UiSpecs<W> colorized() {
    return add(new ShellSpec.Colorized(asyncUi, SWT_DARK_COLORS));
  }

  public UiSpecs<W> tabOrder() {
    return add(new ShellSpec.TabOrder(asyncUi));
  }

  public UiSpecs<W> tags() {
    return add(new Tags(asyncUi));
  }

  public UiSpecs<W> delayClose() {
    return add(new ShellSpec.DelayClose(asyncUi));
  }

  @SuppressWarnings("unchecked")
  private UiSpecs<W> add(Spec spec) {
    this.specs.add(spec);
    refCount.incrementAndGet();
    return this;
  }
}
