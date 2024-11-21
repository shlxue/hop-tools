package org.apache.hop.testing.params;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;
import java.util.function.Predicate;

class WidgetParamResolver extends BaseUiTypeParamResolver<Widget, Void, Composite> {

  WidgetParamResolver() {
    super("SWT", false);
  }

  @Override
  protected boolean matchParamType(Class<?> supportedClazz, Class<?> supportedInterface) {
    return Void.class.equals(supportedInterface);
  }

  @Override
  protected Widget create(Constructor<Widget> constructor, Object parent, ExtensionContext context)
      throws ReflectiveOperationException {
    return constructor.newInstance(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
  }

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return sameParams(constructor, Composite.class, int.class)
        || isWidgetConstructor(constructor, Composite.class::isAssignableFrom);
  }

  private boolean isWidgetConstructor(
      Constructor<?> constructor, Predicate<Class<?>> parentMatcher) {
    Class<?>[] params = constructor.getParameterTypes();
    return params.length == 2
        && parentMatcher.test(params[0])
        && int.class.isAssignableFrom(params[1]);
  }
}
