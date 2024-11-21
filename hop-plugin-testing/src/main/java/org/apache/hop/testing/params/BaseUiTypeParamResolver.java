package org.apache.hop.testing.params;

import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;
import java.util.regex.Pattern;

abstract class BaseUiTypeParamResolver<T, I, P extends Composite>
    extends GenericTypeParamResolver<T, I> {
  static final Pattern HOP_UI_INTERFACE = Pattern.compile("^org.apache.hop.+\\.I[A-Z]\\w+Dialog");
  private final String category;

  public BaseUiTypeParamResolver(String category, boolean forceMatch) {
    super(forceMatch);
    this.category = category;
  }

  @Override
  protected T creator(Constructor<T> constructor, ExtensionContext context)
      throws ReflectiveOperationException {
    Class<?>[] params = constructor.getParameterTypes();
    Object parent;
    int childCount = 0;
    if (Shell.class.isAssignableFrom(params[0]) || Composite.class.equals(params[0])) {
      parent = StatusUtil.get(context, StoreKey.HOP_SWT_NEW_SHELL, Shell.class);
      // newShell(getShell(), constructor.getDeclaringClass());
      childCount = ((Shell) parent).getShells().length;
    } else {
      parent = getParent();
    }
    try {
      return create(constructor, parent, context);
    } finally {
      if (parent instanceof Shell shell) {
        shell.setData("CHILD_SHELL_COUNT", shell.getShells().length - childCount);
      }
    }
  }

  protected Shell getShell() {
    return StatusUtil.get(context, StoreKey.HOP_SWT_SHELL, Shell.class);
  }

  @SuppressWarnings("unchecked")
  protected P getParent() {
    return (P) StatusUtil.get(context, "swt.context", Object.class);
  }

  protected abstract T create(Constructor<T> constructor, Object parent, ExtensionContext context)
      throws ReflectiveOperationException;

  //        .text(String.format("[%s] %s Preview", targetClass.getSimpleName(), category))
}
