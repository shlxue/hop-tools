package org.apache.hop.ui.util;

import org.apache.hop.core.Const;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.database.dialog.DatabaseExplorerDialog;
import org.apache.hop.ui.core.dialog.EnterSelectionDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class Listeners {
  private static final String HOP_KEY_PIPELINE_META = "hop.pipelineMeta";
  private static final String HOP_GETTER_KEY_DATABASE_META = "hop.databaseMeta";
  private static final String HOP_KEY_VARIABLES = "hop.variables";
  private static final String WIDGET_KEY_CONNECTION = "swt.connection";
  private static final String WIDGET_KEY_SCHEMA = "swt.schema";
  private static final String WIDGET_KEY_TABLE = "swt.table";
  private static final String HOP_WIDGET_GETTER = "hop.ui.getter";
  private static final String HOP_WIDGET_SETTER = "hop.ui.setter";

  private Listeners() {}

  static final Class<?> PKG = Listeners.class;

  public static void onSelectedDir(SelectionEvent event) {
    Control widget = Control.class.cast(event.widget);
    String[] fileExtensions = new String[] {"*"};
    String[] fileNames = new String[] {"*"};
    VfsConfig.Builder builder = VfsConfig.of("sel");
    String basePath = Data.getValue(widget, String.class);
    if (basePath != null) {
      builder.basePath(basePath);
    }
    HopHelper.directoryDialog(widget.getShell(), builder.build())
        .ifPresent(path -> Data.setValue(widget, path));
  }

  public static void onSelectedFile(SelectionEvent event) {
    Control widget = Control.class.cast(event.widget);
    String[] fileExtensions = new String[] {"*"};
    String[] fileNames = new String[] {"*"};
    //    IVariables variables = ;
    HopHelper.fileDialog(widget.getShell(), VfsConfig.of("sel").build())
        .ifPresent(path -> Data.setValue(widget, path));
  }

  private static <T> T get(Widget widget, String key, Class<T> clazz) {
    return clazz.cast(widget.getData(key));
  }

  private static Optional<String> schemaDialog(Shell shell, Database database, String schema)
      throws SQLException, HopDatabaseException {
    String[] schemas = database.getSchemas();
    if (null != schemas && schemas.length > 0) {
      database.getConnection().getSchema();
      schemas = Const.sortStrings(schemas);
      String name = database.getDatabaseMeta().getName();
      EnterSelectionDialog dialog =
          new EnterSelectionDialog(
              shell,
              schemas,
              BaseMessages.getString(PKG, "DeleteDialog.AvailableSchemas.Title", name),
              BaseMessages.getString(PKG, "DeleteDialog.AvailableSchemas.Message", name));
      return Optional.ofNullable(dialog.open(Arrays.asList(schemas).indexOf(schema)));
    }
    return Optional.empty();
  }

  private static void errorDialog(Shell shell, Throwable error) {
    new ErrorDialog(
        shell,
        BaseMessages.getString(PKG, "System.Dialog.Error.Title"),
        BaseMessages.getString(PKG, "DeleteDialog.ErrorGettingSchemas"),
        error);
  }

  public static void onSelectedSchema(SelectionEvent event) {
    Widget widget = event.widget;
    IVariables var = get(widget, Data.UI_HOP_VARIABLES, IVariables.class);
    PipelineMeta pipelineMeta = get(widget, Data.UI_HOP_PIPELINE_META, PipelineMeta.class);
    ILoggingObject logObject = get(widget, Data.UI_HOP_LOG, ILoggingObject.class);
    CCombo wConnect = get(widget, Data.UI_JDBC_CONNECTION, CCombo.class);
    String schema = Data.getGetter(widget, String.class, Data.UI_EDITOR_GETTER);
    if (StringUtil.isEmpty(wConnect.getText())) {
      return;
    }
    Shell shell = widget.getDisplay().getActiveShell();
    JdbcHelper.of(logObject, var, pipelineMeta.findDatabase(wConnect.getText(), var))
        .query(
            db -> schemaDialog(shell, db, schema).ifPresent(s -> Data.setValue(widget, s)),
            err -> errorDialog(shell, err));
  }

  private static Optional<String> tableDialog(
      Shell shell, Database database, String schema, String table) throws HopDatabaseException {
    String[] tables =
        Const.sortStrings(
            StringUtil.isEmpty(schema)
                ? database.getTablenames(true)
                : database.getTablenames(schema, false));
    String name = database.getDatabaseMeta().getName();
    EnterSelectionDialog dialog =
        new EnterSelectionDialog(
            shell,
            tables,
            BaseMessages.getString(PKG, "DeleteDialog.AvailableSchemas.Title", name),
            BaseMessages.getString(PKG, "DeleteDialog.AvailableSchemas.Message", name));
    return Optional.ofNullable(dialog.open(Arrays.asList(tables).indexOf(table)));
  }

  public static void onSelectedTable(SelectionEvent event) {
    Widget widget = event.widget;
    IVariables var = get(event.widget, Data.UI_HOP_VARIABLES, IVariables.class);
    PipelineMeta pipelineMeta = get(event.widget, Data.UI_HOP_PIPELINE_META, PipelineMeta.class);
    ILoggingObject logObject = get(event.widget, Data.UI_HOP_LOG, ILoggingObject.class);
    CCombo wConnect = get(event.widget, Data.UI_JDBC_CONNECTION, CCombo.class);
    String schema = Data.get(event.widget, Text.class, Data.UI_JDBC_SCHEMA).getText();
    String table = Data.getGetter(event.widget, String.class, Data.UI_EDITOR_GETTER);
    if (StringUtil.isEmpty(wConnect.getText())) {
      return;
    }
    Shell shell = widget.getDisplay().getActiveShell();
    JdbcHelper.of(logObject, var, pipelineMeta.findDatabase(wConnect.getText(), var))
        .query(
            db -> tableDialog(shell, db, schema, table).ifPresent(s -> Data.setValue(widget, s)),
            err -> errorDialog(shell, err));
  }

  public static SelectionListener selectSchema(
      IVariables variables, PipelineMeta transMeta, CCombo wConnection, Text wSchema) {
    return new SchemaSelectListener(variables, transMeta, wConnection, wSchema);
  }

  public static SelectionListener selectTable(
      IVariables variables, PipelineMeta transMeta, CCombo wConnection, Text wSchema, Text wTable) {
    return new TableSelect(variables, transMeta, wConnection, wSchema, wTable);
  }

  public interface SampleCall {
    void call();
  }

  public static void bindSchemaSelector(
      IVariables variables,
      Supplier<DatabaseMeta> databaseGetter,
      TextVar textVar,
      Widget wSelectButton) {
    wSelectButton.setData(HOP_KEY_VARIABLES, variables);
    wSelectButton.setData(HOP_GETTER_KEY_DATABASE_META, databaseGetter);
    wSelectButton.setData(WIDGET_KEY_SCHEMA, textVar);
  }

  public static void bindTableSelector(
      IVariables variables,
      Supplier<DatabaseMeta> databaseGetter,
      Supplier<String> schemaGetter,
      TextVar textVar,
      Widget wSelectButton) {
    wSelectButton.setData(HOP_KEY_VARIABLES, variables);
    wSelectButton.setData(HOP_GETTER_KEY_DATABASE_META, databaseGetter);
    wSelectButton.setData(WIDGET_KEY_SCHEMA, schemaGetter);
    wSelectButton.setData(WIDGET_KEY_TABLE, textVar);
  }

  public static void bindSchemaSelector(
      IVariables variables,
      PipelineMeta pipelineMeta,
      Supplier<String> connectGetter,
      TextVar textVar,
      Widget wSelectButton) {
    wSelectButton.setData(HOP_KEY_VARIABLES, variables);
    wSelectButton.setData(HOP_KEY_PIPELINE_META, pipelineMeta);
    wSelectButton.setData(WIDGET_KEY_CONNECTION, connectGetter);
    wSelectButton.setData(WIDGET_KEY_SCHEMA, textVar);
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
