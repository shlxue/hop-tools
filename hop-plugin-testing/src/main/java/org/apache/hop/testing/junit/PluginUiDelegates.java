package org.apache.hop.testing.junit;

import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.eclipse.swt.widgets.Shell;

final class PluginUiDelegates {
  public static final String META_NAME_SUFFIX = "-for-test";
  private final Shell parent;
  private final IVariables variables;
  private final ILogChannel log = LogChannel.UI;
  private final MetaDelegate metaDelegate;

  public PluginUiDelegates(HopJunit hopJunit, MetaDelegate metaDelegate) {
    this.metaDelegate = metaDelegate;
    this.parent = hopJunit.getSwtContext().getShell();
    this.variables = hopJunit.getVariables();
  }

  private String genTransformName(Class<? extends ITransformMeta> transformMeta) {
    return transformMeta.getSimpleName() + META_NAME_SUFFIX;
  }

  public <Meta extends ITransformMeta, UI extends ITransformDialog> UI newTransformDialog(
      Class<Meta> metaClass, Class<UI> dialogClass) {
    Meta transformMeta = metaDelegate.newTransformMeta(metaClass);
    String transformName = genTransformName(transformMeta.getClass());
    //    PluginRegistry registry = PluginRegistry.getInstance();
    //    IPlugin plugin = registry.getPlugin(TransformPluginType.class, transformMeta);
    //    if (plugin == null) {
    //      throw new HopException("Missing transform plugin for '" + transformName + "'");
    //    }

    String dialogClassName = transformMeta.getDialogClassName();
    // = plugin.getClassMap().get(ITransformDialog.class);
    if (dialogClassName == null) {
      throw new IllegalStateException(
          String.format(
              "Unable to find dialog class for plugin '%s' : %s",
              dialogClassName, metaClass.getName()));
    }

    PipelineMeta pipelineMeta = metaDelegate.newPipelineMeta(transformMeta, transformName);
    try {
      return dialogClass
          .getConstructor(
              Shell.class, IVariables.class, metaClass, PipelineMeta.class, String.class)
          .newInstance(parent, variables, transformMeta, pipelineMeta, transformName);
    } catch (Exception e) {
      log.logMinimal(
          "Error creating transform ui by (Shell, IVariables, {0}, PipelineMeta, String): {1}",
          metaClass.getSimpleName(), e.getMessage());
    }
    try {
      return dialogClass
          .getConstructor(
              Shell.class, IVariables.class, Object.class, PipelineMeta.class, String.class)
          .newInstance(parent, variables, transformMeta, pipelineMeta, transformName);
    } catch (Exception e) {
      log.logMinimal("Error creating transform ui by (): {0}", e.getMessage());
    }
    try {
      return dialogClass
          .getConstructor(Shell.class, IVariables.class, metaClass, PipelineMeta.class)
          .newInstance(parent, variables, transformMeta, pipelineMeta);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  //  public <T extends IAction> IActionDialog newActionDialog(Class<T> action) {
  //    Object[] arguments =
  //        new Object[] {hopJunit.getShell(), action, workflowMeta, workflowGraph.getVariables()};
  //
  //    if (MissingAction.ID.equals(action.getPluginId())) {
  //      return new MissingActionDialog(
  //          hopJunit.getShell(), action, workflowMeta, workflowGraph.getVariables());
  //    }
  //
  //    PluginRegistry registry = PluginRegistry.getInstance();
  //    IPlugin plugin = registry.getPlugin(ActionPluginType.class, action);
  //    String dialogClassName = action.getDialogClassName();
  //    if (dialogClassName == null) {
  //
  //      // optimistic: simply Dialog added to the action class
  //      //
  //      // org.apache.hop.workflow.actions.ActionZipFile
  //      //
  //      // gives
  //      //
  //      // org.apache.hop.workflow.actions.ActionZipFileDialog
  //      //
  //
  //      dialogClassName = action.getClass().getCanonicalName();
  //      dialogClassName += "Dialog";
  //
  //      try {
  //        // Try by injecting ui into the package. Convert:
  //        //
  //        // org.apache.hop.workflow.actions.ActionZipFileDialog
  //        //
  //        // into
  //        //
  //        // org.apache.hop.ui.workflow.actions.ActionZipFileDialog
  //        //
  //        ClassLoader pluginClassLoader = registry.getClassLoader(plugin);
  //        String alternateName = dialogClassName.replaceFirst("\\.hop\\.", ".hop.ui.");
  //        Class<?> clazz = pluginClassLoader.loadClass(alternateName);
  //        dialogClassName = clazz.getName();
  //      } catch (Exception e) {
  //        // do nothing and return the optimistic plugin classname
  //      }
  //    }
  //
  //    try {
  //      Class<IActionDialog> dialogClass = registry.getClass(plugin, dialogClassName);
  //      Constructor<IActionDialog> dialogConstructor =
  //          dialogClass.getConstructor(
  //              new Class<?>[] {
  //                Shell.class, action.getClass(), WorkflowMeta.class, IVariables.class
  //              });
  //      IActionDialog actionDialog = dialogConstructor.newInstance(arguments);
  //      actionDialog.setMetadataProvider(hopJunit.getMetadataProvider());
  //      return actionDialog;
  //    } catch (Throwable t) {
  //      // do nothing and try an other alternative
  //    }
  //
  //    try {
  //      // TODO: To remove in future version, try old parameters version (before 2.10)
  //      Class<IActionDialog> dialogClass = registry.getClass(plugin, dialogClassName);
  //      Constructor<IActionDialog> dialogConstructor =
  //          dialogClass.getConstructor(
  //              new Class<?>[] {Shell.class, IAction.class, WorkflowMeta.class,
  // IVariables.class});
  //      IActionDialog actionDialog = dialogConstructor.newInstance(arguments);
  //      actionDialog.setMetadataProvider(hopJunit.getMetadataProvider());
  //      return actionDialog;
  //    } catch (Throwable t) {
  //      String errorTitle =
  //          BaseMessages.getString(PKG, "HopGui.Dialog.ErrorCreatingWorkflowDialog.Title");
  //      String errorMsg =
  //          BaseMessages.getString(
  //              PKG, "HopGui.Dialog.ErrorCreatingActionDialog.Message", dialogClassName);
  //      hopJunit.getLog().logError(errorMsg);
  //      new ErrorDialog(parent, errorTitle, errorMsg, t);
  //    }
  //
  //    return null;
  //  }
}
