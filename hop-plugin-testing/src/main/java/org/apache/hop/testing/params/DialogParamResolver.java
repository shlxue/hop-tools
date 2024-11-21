package org.apache.hop.testing.params;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class DialogParamResolver extends BaseUiTypeParamResolver<Dialog, Void, Shell> {

  public DialogParamResolver() {
    super("SWT", false);
  }

  @Override
  protected boolean matchParamType(Class<?> supportedClazz, Class<?> supportedInterface) {
    return Void.class.equals(supportedInterface) && Dialog.class.isAssignableFrom(supportedClazz);
  }

  @Override
  protected boolean excludeSuperClass(Class<?> clazz) {
    return HOP_UI_INTERFACE.matcher(clazz.getName()).matches();
  }

  @Override
  protected Dialog create(Constructor<Dialog> constructor, Object parent, ExtensionContext context)
      throws ReflectiveOperationException {
    if (constructor.getParameterCount() == 1) {
      return constructor.newInstance(parent);
    }
    if (constructor.getParameterCount() == 2) {
      return constructor.newInstance(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
    }
    throw new ReflectiveOperationException(
        "Don't support constructor, param count: " + constructor.getParameterCount());
  }

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return sameParams(constructor, Shell.class) || sameParams(constructor, Shell.class, int.class);
  }
}
