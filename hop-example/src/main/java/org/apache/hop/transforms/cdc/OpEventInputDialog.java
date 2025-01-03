package org.apache.hop.transforms.cdc;

import org.apache.hop.core.Const;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.transforms.cdc.jdbc.MetadataUtil;
import org.apache.hop.transforms.cdc.jdbc.Tab;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.widget.MetaSelectionLine;
import org.apache.hop.ui.core.widget.StyledTextComp;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.*;
import org.apache.hop.ui.widgets.Adapter;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class OpEventInputDialog extends BaseTransformDialog {
  private static final Class<?> PKG = OpEventInputMeta.class;

  private final MetaSelectionLine<DatabaseMeta> wConnection;
  private final TextVar wSchema;
  private final TextVar wTable;
  private final TextVar wKeyFields;
  private final Button wIgnoreDel;
  private final Button wLegacyJoinType;

  private final Table wIndex;
  private final Table wField;
  private final StyledTextComp wCondition;
  private final StyledTextComp wPreviewSql;
  private final Spinner wRowLimit;
  private final Button wOrder;
  private final Button wRefresh;

  private final OpEventInputMeta input;
  private final Tab<IValueMeta> masterTab = Tab.build(null, IValueMeta::getName);

  public OpEventInputDialog(
      Shell parent, IVariables var, OpEventInputMeta meta, PipelineMeta pipelineMeta) {
    super(parent, var, meta, pipelineMeta);
    this.input = meta;
    super.changed = input.hasChanged();

    int middle = props.getMiddlePct();
    int style = SWT.SINGLE | SWT.LEFT | SWT.BORDER;

    super.shell =
        Widgets.shell(SWT.SHELL_TRIM | SWT.BORDER)
            .text(BaseMessages.getString(PKG, "OpEventInput.Title"))
            .layout(Layouts.defaultForm())
            .create(parent);

    super.wTransformName =
        Widgets.text(style)
            .text(transformName)
            .layoutData(LData.byTop(null))
            .onModify(this::onChanged)
            .create(shell);
    super.wlTransformName =
        Widgets.label(SWT.RIGHT)
            .text(BaseMessages.getString(PKG, "System.Label.TransformName"))
            .layoutData(LData.on(wTransformName))
            .create(shell);

    wConnection = addConnectionLine(shell, wTransformName, input.getConnection(), this::onChanged);

    Button wButton =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString(PKG, "System.Button.Browse"))
            .layoutData(LData.toRight(wConnection))
            .onSelect(Listeners::onSelectedSchema)
            .create(shell);
    wSchema =
        Widgets.ofVar(new TextVar(var, shell, style), TextVar::getTextWidget)
            .message("Schema name")
            .layoutData(LData.byRight(wButton))
            .onModify(this::onChanged)
            .onModify(this::refreshPreviewSql)
            .apply();
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "CTL.Label.Schema"))
        .layoutData(LData.on(wSchema))
        .create(shell);
    SwtHelper.bind(Type.JDBC_SCHEMA, wButton, wSchema.getTextWidget(), this::getSchema);

    wButton =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString(PKG, "System.Button.Browse"))
            .layoutData(LData.toRight(wSchema))
            .onSelect(Listeners::onSelectedTable)
            .create(shell);
    wTable =
        Widgets.ofVar(new TextVar(var, shell, style), TextVar::getTextWidget)
            .message("Table name")
            .layoutData(LData.byRight(wButton))
            .onModify(this::onChanged)
            .onModify(this::refreshPreviewSql)
            .apply();
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "CTL.Label.MasterTable"))
        .layoutData(LData.on(wTable))
        .create(shell);
    SwtHelper.bind(Type.JDBC_TABLE, wButton, wTable.getTextWidget(), this::getTable);
    HopHelper.bind(
        var,
        pipelineMeta,
        loggingObject,
        SwtHelper.bind(
            wConnection.getComboWidget(), wSchema.getTextWidget(), wTable.getTextWidget()));

    wKeyFields =
        Widgets.ofVar(new TextVar(var, shell, style | SWT.READ_ONLY), TextVar::getTextWidget)
            .message("Key field list")
            .tooltip(BaseMessages.getString(PKG, "CTL.Tooltip.KeyFields"))
            .enabled(false)
            .layoutData(LData.byTop(wButton))
            .apply();
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "CTL.Label.KeyFields"))
        .layoutData(LData.on(wKeyFields))
        .create(shell);

    wIgnoreDel =
        Widgets.button(SWT.CHECK)
            .text(BaseMessages.getString(PKG, "CTL.Label.IgnoreDel"))
            .tooltip(BaseMessages.getString(PKG, "CTL.Tooltip.IgnoreDel"))
            .layoutData(LData.byTop(wKeyFields, false))
            .onSelect(this::onChanged)
            .create(shell);

    wLegacyJoinType =
        Widgets.button(SWT.CHECK)
            .text(BaseMessages.getString(PKG, "CTL.Label.LegacyJoinType"))
            .tooltip(BaseMessages.getString(PKG, "CTL.Tooltip.LegacyJoinType"))
            .layoutData(LData.byTop(wIgnoreDel, false))
            .onSelect(this::onChanged)
            .create(shell);

    CTabFolder wTabFolder = Widgets.C.tabFolder(SWT.BORDER).create(shell);
    Composite wGeneral =
        Widgets.composite(SWT.NONE).layout(Layouts.nestedForm()).create(wTabFolder);
    Composite wTestData =
        Widgets.composite(SWT.NONE).layout(Layouts.nestedForm()).create(wTabFolder);
    for (Control item : Arrays.asList(wGeneral, wTestData)) {
      Widgets.C.tabItem(SWT.NONE).control(item).create(wTabFolder);
    }
    wTabFolder.getItem(0).setText(BaseMessages.getString(PKG, "CTL.TabFolder.General"));
    wTabFolder.getItem(1).setText(BaseMessages.getString(PKG, "CTL.TabFolder.TestData"));
    wTabFolder.setSelection(0);
    wTabFolder.setData("ui.latest", 0);
    wTabFolder.addSelectionListener(Adapter.widgetSelected(this::selectTabFolder));

    style = SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE;
    TableView vIndex =
        new TableView(
            var, wGeneral, style, SwtUtil.indexColInfo(), 0, true, this::onChanged, props);
    vIndex.setSortable(false);
    vIndex.setLayoutData(LData.form().left(middle).top(null).right(100).bottom(30).get());
    wIndex = vIndex.getTable();
    Widgets.label(SWT.NONE)
        .text(BaseMessages.getString(PKG, "CTL.Label.Indexes"))
        .layoutData(LData.onTop(vIndex))
        .create(wGeneral);

    style |= SWT.CHECK;
    TableView vField =
        new TableView(
            var, wGeneral, style, SwtUtil.fieldColInfo(), 0, true, this::onChanged, props);
    vField.setLayoutData(LData.form().left(middle).top(vIndex).right(100).bottom(100).get());
    Widgets.label(SWT.NONE)
        .text(BaseMessages.getString(PKG, "CTL.Label.Fields"))
        .layoutData(LData.onTop(vField))
        .create(wGeneral);
    wField = vField.getTable();

    wCondition =
        new StyledTextComp(
            var, wTestData, SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL, false);
    Widgets.ofVar(wCondition, StyledTextComp::getTextWidget)
        .layoutData(LData.byTop(null, 0.4))
        .onModify(this::onChanged)
        .onModify(this::refreshPreviewSql)
        .apply();
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "CTL.Label.ConditionSql"))
        .layoutData(LData.onTop(wCondition))
        .create(wTestData);

    wPreviewSql =
        new StyledTextComp(
            var,
            wTestData,
            SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY,
            false);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "CTL.Label.PreviewSql"))
        .layoutData(LData.onTop(wPreviewSql))
        .create(wTestData);

    wRowLimit =
        Widgets.spinner(SWT.BORDER)
            .tooltip(BaseMessages.getString(PKG, "CTL.Tooltip.RowLimit"))
            .bounds(1, 100)
            .increment(100, 100)
            .create(wTestData);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "CTL.Label.RowLimit"))
        .layoutData(LData.on(wRowLimit))
        .create(wTestData);

    wOrder =
        Widgets.button(SWT.CHECK)
            .text(BaseMessages.getString(PKG, "CTL.Label.Order"))
            .layoutData(LData.byBottom(null))
            .onSelect(e -> refreshPreviewSql(new ModifyEvent(new Event())))
            .create(wTestData);
    wRowLimit.setLayoutData(LData.byBottom(wOrder, 100 - middle));
    wPreviewSql.setLayoutData(
        LData.form().left(middle).top(wCondition).right(100).bottom(wRowLimit, 0, SWT.TOP).get());

    setShellImage(shell, input);
    wTabFolder.setLayoutData(LData.fill(wLegacyJoinType, shell, 150, false));
    super.wOk =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.OK"))
            .onSelect(this::ok)
            .create(shell);
    wRefresh =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString(PKG, "System.Button.Refresh"))
            .onSelect(this::onRefresh)
            .create(shell);
    super.wPreview =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString(PKG, "System.Button.PreviewRows"))
            .onSelect(this::onPreview)
            .create(shell);
    super.wCancel =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.Cancel"))
            .onSelect(this::cancel)
            .create(shell);

    wIndex.addSelectionListener(Adapter.widgetSelected(this::selectColumn));
    wField.addSelectionListener(Adapter.widgetSelected(this::checkChanged));
    wField.addFocusListener(Adapter.focusLost(this::onFocusLost));

    positionBottomButtons(shell, new Button[] {wOk, wRefresh, wPreview, wCancel}, 0, wTabFolder);
    SwtDialog.preferredShellStyle(shell, wOk);
  }

  public String open() {
    PropsUi.setLook(shell);
    PropsUi.setLook(wIndex, PropsUi.WIDGET_STYLE_TABLE);
    PropsUi.setLook(wField, PropsUi.WIDGET_STYLE_TABLE);
    shell.layout(true, true);

    getData();
    //        setTableFieldCombo();
    input.setChanged(changed);

    SwtDialog.defaultShellHanding(shell, this::ok, this::cancel);
    return transformName;
  }

  private String getCheckedColumnNames() {
    SortedSet<String> names = new TreeSet<>(String::compareToIgnoreCase);
    for (TableItem item : wField.getItems()) {
      if (item.getChecked()) {
        names.add(item.getText(1));
      }
    }
    return String.join(", ", names);
  }

  private void onFocusLost(FocusEvent e) {
    String names = getCheckedColumnNames();
    if (!wKeyFields.getText().equalsIgnoreCase(names)) {
      wKeyFields.setText(names);
    }
  }

  private void checkChanged(SelectionEvent event) {
    if (event.detail != SWT.CHECK) {
      return;
    }
    wKeyFields.setText(getCheckedColumnNames());
    if (wIndex.getSelectionIndex() != -1) {
      wIndex.setSelection(new int[] {-1});
    }
  }

  private void refreshPreviewSql(ModifyEvent event) {
    StringBuilder builder =
        new StringBuilder(256)
            .append("SELECT ")
            .append(wKeyFields.getText())
            .append("\nFROM ")
            .append(wTable.getText())
            .append("\n");
    if (StringUtil.isEmpty(wCondition.getText())) {
      builder.append("-- ");
    }
    builder.append("WHERE ").append(wCondition.getText().trim()).append("\n");
    if (wOrder.getSelection()) {
      builder.append("ORDER BY ").append(wKeyFields.getText());
    }
    wPreviewSql.setText(builder.toString());
  }

  private void selectTabFolder(SelectionEvent event) {
    CTabFolder wTabFolder = (CTabFolder) event.widget;
    CTabItem currentItem = (CTabItem) event.item;
    int latest = (int) wTabFolder.getData("ui.latest");
    int index = wTabFolder.getSelectionIndex();

    if (latest == 0 && index != 0) {
      List<String> names = new ArrayList<>();
      for (TableItem item : wField.getItems()) {
        if (item.getChecked()) {
          names.add(item.getText(1));
        }
      }
      wKeyFields.setText(String.join(OpEventInputMeta.FIELD_SEPARATOR, names));
      Event e = new Event();
      e.widget = wKeyFields;
      e.display = event.display;
      refreshPreviewSql(new ModifyEvent(e));
      //      boolean update = wKeyFields.getItemCount() != names.size();
      //      if (!update) {
      //        List<String> list = meta.getKeyNames();
      //        for (int i = 0; i < list.size(); i++) {
      //          if (!list.get(i).equalsIgnoreCase(wKeyFields.getItem(i, 1))) {
      //            update = true;
      //            break;
      //          }
      //        }
      //      }
      //      if (update) {
      //        fillTableViewer(wKeyFields.getTable(), meta.getKeyFields(), this::fillNameAndType);
      //      }
    }
    wRefresh.setEnabled(index == 0);
    //    wPreview.setEnabled(index == 1);
    wTabFolder.setData("ui.latest", wTabFolder.getSelectionIndex());
  }

  void getData() {
    input.setConnection("local");
    masterTab.refresh(input.getTabModel().clone());
    logDebug(BaseMessages.getString(PKG, "MSG.ApplyMeta", masterTab.getName()));
    wTransformName.setText(Const.nullToEmpty(transformName));
    wConnection.setText(Const.nullToEmpty(input.getConnection()));
    wSchema.setText(Const.nullToEmpty(input.getSchema()));
    wTable.setText(Const.nullToEmpty(input.getTable()));
    wKeyFields.setText(Const.nullToEmpty(input.getPkFields()));
    wIgnoreDel.setSelection(input.isIgnoreDel());
    wLegacyJoinType.setSelection(input.isLegacyJoinType());
    wTable.setData("model.table", input.getTabModel());

    SwtUtil.fillTableView(wField, input.getFields(), SwtUtil::fillField);
    SwtUtil.fillTableView(wIndex, input.getIndexes(), SwtUtil::fillIndex);
    SwtUtil.hintHeight(wIndex);
    String pkFields = input.getPkFields();
    if (!StringUtil.isEmpty(pkFields)) {
      autoSelectColumn(wField.getItems(), input.getPkFields());
    }
    wCondition.setText(Const.nullToEmpty(input.getWhereCondition()));
    wRowLimit.setSelection(input.getRowLimit());
    wOrder.setSelection(input.isOrder());

    wTransformName.selectAll();
  }

  private void autoSelectColumn(TableItem[] items, String keyNames) {
    logDebug(BaseMessages.getString(PKG, "MSG.AutoSelectFields", keyNames));
    for (TableItem item : items) {
      String text = item.getText(1);
      boolean selected = false;
      for (String name : keyNames.split(OpEventInputMeta.FIELD_SEPARATOR)) {
        if (name.trim().equalsIgnoreCase(text)) {
          selected = true;
          break;
        }
      }
      if (Boolean.compare(item.getChecked(), selected) != 0) {
        item.setChecked(selected);
      }
    }
    String names = getCheckedColumnNames();
    if (!names.equalsIgnoreCase(wKeyFields.getText())) {
      wKeyFields.setText(names);
    }
    if (wIndex.getSelectionIndex() == -1) {
      wIndex.setSelection(new int[] {masterTab.getPkIndex()});
    }
  }

  private void selectColumn(SelectionEvent event) {
    if (event.item == null) {
      return;
    }
    TableItem tableItem = (TableItem) event.item;
    List<String> indexFields = new ArrayList<>(Arrays.asList(tableItem.getText(4).split(",")));
    String[] currentFields = wKeyFields.getText().split(",");
    if (indexFields.size() == currentFields.length) {
      indexFields.removeAll(Arrays.asList(currentFields));
    }
    if (!indexFields.isEmpty()) {
      TableItem[] items = wField.getItems();
      autoSelectColumn(items, tableItem.getText(4));
    }
  }

  void ok(SelectionEvent event) {
    OpEventInputMeta model = (OpEventInputMeta) baseTransformMeta;
    model.setConnection(wConnection.getText());
    masterTab.setSchema(wSchema.getText());
    masterTab.setName(wTable.getText());
    model.setIgnoreDel(wIgnoreDel.getSelection());
    model.setLegacyJoinType(wLegacyJoinType.getSelection());

    //    model.setFields(
    //        Arrays.stream(wField.getItems())
    //            .map(item -> (IValueMeta) item.getData("model.field"))
    //            .collect(Collectors.toList()));
    //    model.setIndexes(
    //        Arrays.stream(wIndex.getItems())
    //            .map(item -> (Index<IValueMeta>) item.getData("model.index"))
    //            .collect(Collectors.toList()));

    //    String keyFields = getCheckedColumnNames();
    //    if (keyFields.equalsIgnoreCase(wKeyFields.getText())) {
    //      wKeyFields.setText(keyFields);
    //    }
    masterTab.setPkFieldNames(wKeyFields.getText());
    model.getTabModel().refresh(masterTab, true);

    //      model.setShowAllFields(wbShowAllFields.getSelection());
    //          model.setKeyFields(getKeyFields(wKeyFields.getTable()));
    model.setWhereCondition(wCondition.getText());
    model.setOrder(wOrder.getSelection());
    model.setRowLimit(wRowLimit.getSelection());
    model.setIgnoreDel(wIgnoreDel.getSelection());
    //      model.setSql(wDesignSQL.getText());

    //          model.setDeleteStepName(wDeleteStep.getText());
    //    model.getTransformIOMeta()
    //        .getTargetStreams()
    //        .get(1)
    //        .setTransformMeta(pipelineMeta.findTransform(model.getDeleteStepName()));
    transformName = wTransformName.getText();

    model.setChanged();
    dispose();
  }

  void cancel(SelectionEvent event) {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  void onRefresh(SelectionEvent event) {
    Control self = (Control) event.widget;
    self.setEnabled(false);
    String schema = wSchema.getText();
    String table = wTable.getText();
    Tab<IValueMeta> tab = masterTab;
    DatabaseMeta databaseMeta = pipelineMeta.findDatabase(wConnection.getText(), variables);
    try (Database db = new Database(loggingObject, variables, databaseMeta)) {
      db.connect();
      MetadataUtil util =
          MetadataUtil.getInstance(variables, db.getDatabaseMeta(), db.getConnection());
      tab.setFields(util.getRowMeta(schema, table).getValueMetaList());
      logDebug(BaseMessages.getString(PKG, "MSG.GetFields", table));

      tab.setIndexes(
          util.getIndexes(schema, table, (rowMeta, row) -> SwtUtil.toIndex(tab, rowMeta, row)));
      logDebug(BaseMessages.getString(PKG, "MSG.GetIndexes", table));

      String[] primaryKeys = util.getPrimaryKeys(schema, table);
      if (primaryKeys.length > 0 && !tab.getIndexes().isEmpty()) {
        tab.setPrimaryKeyName(primaryKeys[0]);
        if (!tab.getPrimaryKey().isPresent()) {
          tab.setPrimaryKeyFields(primaryKeys[1]);
        }
      }
      logDebug(BaseMessages.getString(PKG, "MSG.LoadTableSchema", table));
    } catch (SQLException | HopDatabaseException e) {
      throw new IllegalStateException(e);
    } finally {
      self.setEnabled(true);
    }

    SwtUtil.fillTableView(wField, tab.getFields(), SwtUtil::fillField);
    logDebug(
        BaseMessages.getString(PKG, "MSG.ApplyTableField", tab.getFields().size(), tab.getName()));
    SwtUtil.fillTableView(wIndex, tab.getIndexes(), SwtUtil::fillIndex);
    logDebug(
        BaseMessages.getString(PKG, "MSG.ApplyTableIndex", tab.getIndexes().size(), tab.getName()));

    autoSelectColumn(wField.getItems(), wKeyFields.getText());
    String keyFields = getCheckedColumnNames();
    if (wKeyFields.getText().equalsIgnoreCase(keyFields)) {
      wKeyFields.setText(keyFields);
    }
    if (wIndex.getSelectionCount() > 0) {
      wIndex.setSelection(-1);
    }
    //    fillTableViewer(wKeyFi, meta.getKeyFields(), this::fillKey);
    SwtUtil.hintHeight(wIndex);
  }

  void onPreview(SelectionEvent event) {
    OpEventInputMeta meta = (OpEventInputMeta) input.clone();
    meta.setSchema(wSchema.getText());
    meta.setTable(wTable.getText());
    meta.setFields(
        Arrays.stream(wField.getItems())
            .map(item -> (IValueMeta) item.getData("model.field"))
            .collect(Collectors.toList()));
    meta.setKeyNames(wKeyFields.getText());
    meta.setIgnoreDel(wIgnoreDel.getSelection());
    meta.setLegacyJoinType(wLegacyJoinType.getSelection());
    meta.setWhereCondition(wCondition.getText());
    meta.setOrder(wOrder.getSelection());
    meta.setRowLimit(wRowLimit.getSelection());
    logDebug(BaseMessages.getString(PKG, "MSG.PreviewTableData", wTable.getText()));
    //    Listeners.preview(
    //        shell, variables, meta, metadataProvider, wRowLimit.getSelection(), transformName);
  }

  <T> void onChanged(T event) {
    input.setChanged();
  }

  private String getSchema() {
    return variables.resolve(wSchema.getText());
  }

  private String getTable() {
    return variables.resolve(wTable.getText());
  }
}
