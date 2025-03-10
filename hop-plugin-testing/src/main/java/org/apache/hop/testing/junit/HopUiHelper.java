package org.apache.hop.testing.junit;

import org.apache.hop.core.IExtensionData;
import org.apache.hop.core.database.IDatabase;
import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.action.IActionDialog;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Widget;
import org.junit.platform.commons.util.ReflectionUtils;

import java.util.Optional;

public final class HopUiHelper {
  private HopUiHelper() {}

  public static boolean isCorePlugin(Class<?> clazz) {
    return IVariables.class.isAssignableFrom(clazz) || IDatabase.class.isAssignableFrom(clazz);
  }

  public static boolean isSwtWidget(Class<?> clazz) {
    return Widget.class.isAssignableFrom(clazz);
  }

  public static boolean isSwtDialog(Class<?> clazz) {
    return Dialog.class.isAssignableFrom(clazz);
  }

  public static boolean isAction(Class<?> clazz) {
    return IAction.class.isAssignableFrom(clazz);
  }

  public static boolean isTransform(Class<?> clazz) {
    return ITransform.class.isAssignableFrom(clazz);
  }

  public static boolean isPlugin(Class<?> clazz) {
    return IVariables.class.isAssignableFrom(clazz) && IExtensionData.class.isAssignableFrom(clazz);
  }

  public static boolean isPluginUi(Class<?> clazz) {
    return isSwtDialog(clazz) && isPluginDialog(clazz);
  }

  public static boolean isEngine(Class<?> clazz) {
    return !isPlugin(clazz) && INamedParameters.class.isAssignableFrom(clazz);
  }

  public static boolean isActionUi(Class<?> clazz) {
    return IActionDialog.class.isAssignableFrom(clazz);
  }

  public static boolean isTransformUi(Class<?> clazz) {
    return IActionDialog.class.isAssignableFrom(clazz);
  }

  public static Optional<Class<?>> toPluginClass(Class<?> uiClass) {
    if (isUiInterface(uiClass)) {
      String className = uiClass.getName();
      className = className.substring(0, className.length() - 6).replaceFirst("\\.ui\\.", ".");
      return ReflectionUtils.tryToLoadClass(className, uiClass.getClassLoader()).toOptional();
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public static <I, T> Class<I> toSafeType(Class<I> iClass, Class<T> clazz, Class<Dialog> uiClass) {
    return (Class<I>) (iClass.equals(clazz) ? toPluginClass(uiClass).orElse(clazz) : clazz);
  }

  public static boolean isPluginDialog(Class<?> uiClass) {
    if (isUiInterface(uiClass)) {
      Optional<Class<?>> optional = toPluginClass(uiClass);
      return optional.isPresent() && isPlugin(optional.get());
    }
    return false;
  }

  private static boolean isUiInterface(Class<?> uiClass) {
    if (isSwtDialog(uiClass) && uiClass.getSimpleName().endsWith("Dialog")) {
      Class<?> ref = uiClass;
      while (ref != null) {
        for (Class<?> item : ref.getInterfaces()) {
          String name = item.getSimpleName();
          if (name.startsWith("I") && name.endsWith("Dialog")) {
            return true;
          }
        }
        ref = ref.getSuperclass();
      }
    }
    return false;
  }
}
