package org.apache.hop.ui.util;

import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.PipelinePreviewFactory;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.EnterNumberDialog;
import org.apache.hop.ui.core.dialog.EnterTextDialog;
import org.apache.hop.ui.core.dialog.PreviewRowsDialog;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.pipeline.dialog.PipelinePreviewProgressDialog;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.Optional;
import java.util.function.Function;

public class HopHelper {
  static final Class<?> PKG = Listeners.class;

  private HopHelper() {}


  public static Optional<String> fileDialog(Shell shell, VfsConfig cfg) {
//    FileDialog dialog = new FileDialog(shell);
//    if (!isEmpty(cfg.getTitle())) {
//      dialog.setText(cfg.getTitle());
//    }
//    if (!isEmpty(cfg.getBasePath())) {
//      dialog.setFilterPath(cfg.getBasePath());
//    }
    return Optional.empty();
  }

  static Optional<String> directoryDialog(Shell shell, VfsConfig cfg) {
    IVariables variables = null;
    BaseDialog.presentDirectoryDialog(shell, variables);
//    DirectoryDialog dialog = new DirectoryDialog(shell);
//    if (!isEmpty(cfg.getTitle())) {
//      dialog.setText(cfg.getTitle());
//    }
//    if (!isEmpty(cfg.getMessage())) {
//      dialog.setMessage(cfg.getMessage());
//    }
//    if (!isEmpty(cfg.getBasePath())) {
//      dialog.setFilterPath(cfg.getBasePath());
//    }
    return Optional.empty();
  }

  public static void bind(
      IVariables variables, PipelineMeta pipelineMeta, ILoggingObject parent, Control... controls) {
    for (Control item : controls) {
      Data.add(item, Data.UI_HOP_VARIABLES, variables);
      Data.add(item, Data.UI_HOP_PIPELINE_META, pipelineMeta);
      Data.add(item, Data.UI_HOP_LOG, parent);
    }
  }

  public static CCombo reLayoutConnectionLine(
      Composite parent, Function<Composite, CCombo> creator) {
    Composite wConnectionLine =
        Widgets.composite(SWT.NONE)
            .layout(Layouts.grid(4).margin(0).clean(true, true).get())
            //            .layoutData(LData.field())
            .create(parent);

    Composite wTemp = Widgets.composite(SWT.NONE).create(parent);
    CCombo wCCombo = creator.apply(wTemp);

    Control[] children = wTemp.getChildren();
    children[4].setParent(wConnectionLine);
    children[3].setParent(wConnectionLine);
    children[2].setParent(wConnectionLine);
    children[1].setParent(wConnectionLine);

    children = wConnectionLine.getChildren();
    children[0].setLayoutData(LData.grid(GridData.FILL_HORIZONTAL).get());
    children[1].setLayoutData(LData.grid().hint(120).get());
    children[2].setLayoutData(LData.grid().hint(120).get());
    children[3].setLayoutData(LData.grid().hint(120).get());

    wTemp.dispose();
    return wCCombo;
  }

  public static void preview(
      Shell shell,
      IVariables variables,
      ITransformMeta stepMeta,
      IHopMetadataProvider provider,
      int limit,
      String stepName) {
    PipelineMeta previewMeta =
        PipelinePreviewFactory.generatePreviewPipeline(provider, stepMeta, stepName);

    int previewSize = limit;
    if (limit < 0) {
      EnterNumberDialog numberDialog =
          new EnterNumberDialog(
              shell,
              -limit,
              BaseMessages.getString(PKG, "UIHelper.EnterPreviewSize"),
              BaseMessages.getString(PKG, "UIHelper.NumberOfRowsToPreview"));
      previewSize = numberDialog.open();
      if (previewSize <= 0) {
        return;
      }
    }
    HopLogStore.getAppender().clear();
    PipelinePreviewProgressDialog progressDialog =
        new PipelinePreviewProgressDialog(
            shell, variables, previewMeta, new String[] {stepName}, new int[] {previewSize});
    progressDialog.open();

    Pipeline trans = progressDialog.getPipeline();
    String loggingText = progressDialog.getLoggingText();

    if (progressDialog.isCancelled()) {
      return;
    }
    if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
      String title = BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title");
      String message = BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message");
      EnterTextDialog etd = new EnterTextDialog(shell, title, message, loggingText, true);
      etd.setReadOnly();
      etd.open();
    } else {
      PreviewRowsDialog prd =
          new PreviewRowsDialog(
              shell,
              variables,
              SWT.NONE,
              stepName,
              progressDialog.getPreviewRowsMeta(stepName),
              progressDialog.getPreviewRows(stepName),
              loggingText);
      prd.open();
    }
  }
}
