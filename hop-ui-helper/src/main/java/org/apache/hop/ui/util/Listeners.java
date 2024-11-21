package org.apache.hop.ui.util;

import org.apache.hop.core.Const;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.PipelinePreviewFactory;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.ui.core.database.dialog.DatabaseExplorerDialog;
import org.apache.hop.ui.core.dialog.EnterNumberDialog;
import org.apache.hop.ui.core.dialog.EnterSelectionDialog;
import org.apache.hop.ui.core.dialog.EnterTextDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.dialog.PreviewRowsDialog;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.pipeline.dialog.PipelinePreviewProgressDialog;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Listeners {

  static final Class<?> PKG = Listeners.class;

  public static ShellListener shellClosedListener(SampleCall cancel) {
    return new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        cancel.call();
      }
    };
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

  public static SelectionListener selectSchema(
      IVariables variables, PipelineMeta transMeta, CCombo wConnection, Text wSchema) {
    return new SchemaSelectListener(variables, transMeta, wConnection, wSchema);
  }

  public static SelectionListener selectTable(
      IVariables variables, PipelineMeta transMeta, CCombo wConnection, Text wSchema, Text wTable) {
    return new TableSelect(variables, transMeta, wConnection, wSchema, wTable);
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

  public interface SampleCall {
    void call();
  }

  private static class SchemaSelectListener extends SelectionAdapter {

    final IVariables variables;
    final PipelineMeta transMeta;
    final CCombo wConnection;
    final Text wSchema;
    final ILoggingObject loggingObject;

    SchemaSelectListener(
        IVariables variables, PipelineMeta transMeta, CCombo wConnection, Text wSchema) {
      this.variables = variables;
      this.transMeta = transMeta;
      this.wConnection = wConnection;
      this.wSchema = wSchema;
      this.loggingObject = BaseTransformDialog.loggingObject;
    }

    @Override
    public void widgetSelected(SelectionEvent selectionEvent) {
      DatabaseMeta databaseMeta = transMeta.findDatabase(wConnection.getText(), variables);
      if (databaseMeta == null) {
        return;
      }
      Shell shell = wConnection.getShell();
      try (Database database = new Database(loggingObject, variables, databaseMeta)) {
        database.connect();
        loading(wConnection.getShell(), database, wSchema.getText());
      } catch (Exception e) {
        new ErrorDialog(
            shell,
            BaseMessages.getString(PKG, "System.Dialog.Error.Title"),
            BaseMessages.getString(PKG, "UIHelper.ErrorGettingSchemas"),
            e);
      }
    }

    protected void loading(Shell shell, Database database, String selected)
        throws SQLException, HopDatabaseException {
      DatabaseMetaData metaData = database.getConnection().getMetaData();
      String[] schemas = getSchemas(metaData, database::getCatalogs, database::getSchemas);
      if (schemas == null || schemas.length == 0) {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
        mb.setMessage(BaseMessages.getString(PKG, "UIHelper.NoSchema.Warning"));
        mb.setText(BaseMessages.getString(PKG, "System.Warning"));
        mb.open();
        return;
      }
      EnterSelectionDialog dialog =
          new EnterSelectionDialog(
              shell,
              Const.sortStrings(schemas),
              BaseMessages.getString(
                  PKG, "System.Dialog.AvailableSchemas.Title", wConnection.getText()),
              BaseMessages.getString(
                  PKG, "System.Dialog.AvailableSchemas.Message", wConnection.getText()));
      if (StringUtil.isEmpty(selected)) {
        if (metaData.supportsSchemasInTableDefinitions()) {
          selected = database.getConnection().getSchema();
        } else if (metaData.supportsCatalogsInTableDefinitions()) {
          selected = database.getConnection().getCatalog();
        }
      }
      //      selected = dialog.open(ArrayUtils.indexOf(schemas, selected));
      if (dialog.getSelectionIndeces() != null && dialog.getSelectionNr() >= 0) {
        wSchema.setText(Const.nullToEmpty(selected));
      }
    }
  }

  private String getSchema(
      DatabaseMetaData metaData, Supplier<String> category, Supplier<String> schema)
      throws HopDatabaseException {
    if (supportsSchemasInTable(metaData)) {
      return schema.get();
    } else if (supportsCatalogsInTable(metaData)) {
      return category.get();
    }
    return "";
  }

  static <T> T getSchemas(DatabaseMetaData metaData, Callable<T> category, Callable<T> schema)
      throws HopDatabaseException {
    if (supportsSchemasInTable(metaData)) {
      return callMetaData(schema);
    } else if (supportsCatalogsInTable(metaData)) {
      return callMetaData(category);
    }
    return null;
  }

  private static <T> T callMetaData(Callable<T> supplier) throws HopDatabaseException {
    try {
      return supplier.call();
    } catch (HopDatabaseException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static boolean supportsSchemasInTable(DatabaseMetaData metaData)
      throws HopDatabaseException {
    return callMetaData(metaData::supportsSchemasInTableDefinitions);
  }

  private static boolean supportsCatalogsInTable(DatabaseMetaData metaData)
      throws HopDatabaseException {
    return callMetaData(metaData::supportsCatalogsInTableDefinitions);
  }

  private static class TableSelect extends SchemaSelectListener {
    private final Text wTable;

    TableSelect(
        IVariables variables,
        PipelineMeta transMeta,
        CCombo wConnection,
        Text wSchema,
        Text wTable) {
      super(variables, transMeta, wConnection, wSchema);
      this.wTable = wTable;
    }

    @Override
    protected void loading(Shell shell, Database database, String selected)
        throws SQLException, HopDatabaseException {
      DatabaseExplorerDialog std =
          new DatabaseExplorerDialog(
              shell, SWT.NONE, variables, database.getDatabaseMeta(), transMeta.getDatabases());
      String schema = wSchema.getText();
      if (StringUtil.isEmpty(schema)) {
        Connection connection = database.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        if (metaData.supportsSchemasInTableDefinitions()) {
          schema = connection.getSchema();
        } else if (metaData.supportsCatalogsInTableDefinitions()) {
          schema = connection.getCatalog();
        }
      }
      std.setSelectedSchemaAndTable(schema, wTable.getText());
      if (std.open()) {
        wSchema.setText(Const.nullToEmpty(std.getSchemaName()));
        wTable.setText(Const.nullToEmpty(std.getTableName()));
      }
    }
  }
}
