package org.apache.hop.testing.params;

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.testing.junit.HopJunit;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class TransformUiParamResolver extends BaseUiTypeParamResolver<Dialog, ITransformDialog, Shell> {

  TransformUiParamResolver() {
    super("Transform", true);
  }

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    Class<?>[] params = constructor.getParameterTypes();
    return params.length > 3
        && Shell.class.equals(params[0])
        && IVariables.class.equals(params[1])
        && ITransformMeta.class.isAssignableFrom(params[2])
        && PipelineMeta.class.equals(params[3]);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Dialog create(Constructor<Dialog> constructor, Object parent, ExtensionContext context)
      throws ReflectiveOperationException {
    HopJunit hopJunit = StatusUtil.get(context, StoreKey.HOP_JUNIT, HopJunit.class);

    Class<?>[] paramTypes = constructor.getParameterTypes();
    ITransformMeta transformMeta =
        hopJunit.newTransformMeta((Class<? extends ITransformMeta>) paramTypes[2]);
    Dialog dialog =
        constructor.newInstance(
            parent,
            hopJunit.newVariables(),
            transformMeta,
            transformMeta.getParentTransformMeta().getParentPipelineMeta());
    if (dialog instanceof ITransformDialog pluginUi) {
      pluginUi.setMetadataProvider(hopJunit.getMetadataProvider());
    }
    return dialog;
  }
}
