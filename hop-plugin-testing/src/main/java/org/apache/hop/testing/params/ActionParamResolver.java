package org.apache.hop.testing.params;

import org.apache.hop.workflow.action.IAction;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class ActionParamResolver<T extends IAction> extends BasePluginParam<T, Void> {

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return false;
  }

  @Override
  protected T creator(Constructor<T> constructor, ExtensionContext context)
      throws ReflectiveOperationException {
    return null;
  }
}
