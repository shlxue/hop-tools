package org.apache.hop.testing.params;

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.testing.junit.HopJunit;
import org.apache.hop.testing.junit.HopUiHelper;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.action.IActionDialog;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class ActionUiParamResolver extends BaseUiTypeParamResolver<Dialog, IActionDialog, Shell> {

  ActionUiParamResolver() {
    super("Action", true);
  }

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    Class<?>[] params = constructor.getParameterTypes();
    return params.length == 4
        && Shell.class.equals(params[0])
        && IAction.class.isAssignableFrom(params[1])
        && WorkflowMeta.class.equals(params[2])
        && IVariables.class.equals(params[3]);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Dialog create(Constructor<Dialog> constructor, Object parent, ExtensionContext context)
      throws ReflectiveOperationException {
    HopJunit hopJunit = StatusUtil.get(context, StoreKey.HOP_JUNIT, HopJunit.class);
    Class<IAction> type = (Class<IAction>) constructor.getParameterTypes()[1];
    IAction action =
        hopJunit.newAction(
            HopUiHelper.toSafeType(IAction.class, type, constructor.getDeclaringClass()));
    return constructor.newInstance(
        parent, action, action.getParentWorkflowMeta(), hopJunit.newVariables());
  }
}
