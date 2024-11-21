package org.apache.hop.transforms.cdc;

import com.opennews.hop.jdbc.Index;
import com.opennews.hop.jdbc.MetadataUtil;
import com.opennews.hop.jdbc.Tab;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.hop.core.Const;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.util.ExecutorUtil;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.widget.*;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.Listeners;
import org.apache.hop.ui.util.SwtDialog;
import org.apache.hop.ui.widgets.Adapter;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SubOpEventDialog extends BaseTransformDialog {
  static final String MASTER_STEP_ID = "OpEventInput";

  private final Tab<IValueMeta> master = Tab.build(null, IValueMeta::getName);
  private final MetaSelectionLine<DatabaseMeta> wConnection;

  private final TextVar wSchema;
  private final TextVar wMasterTable;
  private final TextVar wKeyFields;
  private final Button wIgnoreNonMap;
  private final Button wIgnoreAllDelOp;

  private final ColumnInfo wTableNames;
  private final TableView wRelationTableView;
  private final Table wRelationTable;
  private final Table wIndex;
  private final Table wField;

  private final StyledTextComp wJoinSql;
  private final StyledTextComp wDesignSql;
  private final StyledTextComp wOrderBy;
  private final Spinner wJoinLimit;
  private final Button wIgnoreDel;

  private final StyledTextComp wLoadingSql;
  private final StyledTextComp wConditionSql;
  private final Spinner wRowLimit;
  private final Button wShowOriginKey;
  private final Button wRefresh;

  private TableItem latestItem;
  private final SubOpEventMeta input;

  public SubOpEventDialog(
      Shell parent, IVariables variables, SubOpEventMeta transformMeta, PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    this.input = transformMeta;
    super.changed = transformMeta.hasChanged();

    super.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE)
            .text(i18n("Step.SubOpEvent.Name"))
            .layout(Layouts.defaultForm())
            .create(parent);

    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();
    ModifyListener lsMod = e -> input.setChanged();

    wTransformName =
        Widgets.text(SWT.SINGLE | SWT.LEFT | SWT.BORDER)
            .tooltip(i18n("System.Tooltip.TransformName"))
            .layoutData(LData.byTop(null))
            .create(shell);
    Widgets.label(SWT.RIGHT)
        .text(i18n("System.Label.TransformName"))
        .layoutData(LData.on(wTransformName))
        .create(shell);

    wConnection = addConnectionLine(shell, wTransformName, input.getConnection(), lsMod);
    wConnection.getComboWidget().setToolTipText(i18n("CTL.ToolTip.Connection"));
    wConnection.setLayoutData(LData.form().left(0).top(wTransformName).right(100).get());

    Button wButton =
        Widgets.button(SWT.PUSH)
            .text(i18n("System.Button.Browse"))
            .layoutData(LData.toRight(wConnection, margin))
            .onSelect(this::selectSchema)
            .create(shell);
    wSchema = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wSchema.setLayoutData(LData.byRight(wButton, margin * 2));
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.Schema"))
        .layoutData(LData.on(wSchema))
        .create(shell);

    wButton =
        Widgets.button(SWT.PUSH)
            .text(i18n("System.Button.Browse"))
            .layoutData(LData.toRight(wButton, margin))
            .onSelect(this::selectTable)
            .create(shell);
    wMasterTable = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wMasterTable.setToolTipText(i18n("CTL.ToolTip.MasterTable"));
    wMasterTable.setLayoutData(LData.byRight(wButton, margin * 2));
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.MasterTable"))
        .layoutData(LData.on(wMasterTable))
        .create(shell);

    wKeyFields = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wKeyFields.setToolTipText(i18n("CTL.ToolTip.KeyFields"));
    wKeyFields.setLayoutData(LData.byTop(wMasterTable));
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.KeyFields"))
        .layoutData(LData.on(wKeyFields))
        .create(shell);

    wIgnoreAllDelOp =
        Widgets.button(SWT.CHECK)
            .text(i18n("CTL.Label.IgnoreAllDelOp"))
            .tooltip(i18n("CTL.Tooltip.IgnoreAllDelOp"))
            .layoutData(LData.byTop(wKeyFields, false))
            .create(shell);

    wIgnoreNonMap =
        Widgets.button(SWT.CHECK)
            .text(i18n("CTL.Label.IgnoreNonMap"))
            .tooltip(i18n("CTL.Tooltip.IgnoreNonMap"))
            .layoutData(LData.byTop(wIgnoreAllDelOp, false))
            .create(shell);

    CTabFolder wTabFolder = new CTabFolder(shell, SWT.BORDER);
    Composite wGeneral = new Composite(wTabFolder, SWT.NONE);
    Composite wJoinEditor = new Composite(wTabFolder, SWT.NONE);
    Composite wTestData = new Composite(wTabFolder, SWT.NONE);
    for (Composite item : Arrays.asList(wGeneral, wJoinEditor, wTestData)) {
      item.setLayout(Layouts.defaultForm());
      Widgets.C.tabItem(SWT.NONE).control(item).create(wTabFolder);
    }
    wTabFolder.getItem(0).setText(i18n("CTL.TabFolder.General"));
    wTabFolder.getItem(1).setText(i18n("CTL.TabFolder.JoinEditor"));
    wTabFolder.getItem(2).setText(i18n("CTL.TabFolder.TestData"));
    wTabFolder.setSelection(0);

    //// General
    int style =
        SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL;

    Widgets.label(SWT.RIGHT).text(i18n("CTL.Label.Fields")).create(wGeneral);
    Label wLabel =
        Widgets.label(SWT.RIGHT)
            .text(i18n("CTL.Label.RelationList"))
            .layoutData(LData.form().left(middle).get())
            .create(wGeneral);
    TableView wFieldView =
        new TableView(variables, wGeneral, style, SwtUtil.fieldColInfo(), 0, true, lsMod, props);
    wFieldView.setSortable(false);
    wFieldView.setReadonly(true);
    wField = wFieldView.getTable();
    wField.setToolTipText(i18n("CTL.ToolTip.Fields"));
    wFieldView.setLayoutData(LData.form().top(wLabel).left(0).right(wLabel).bottom(100).get());

    style =
        SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FOCUSED | SWT.RESIZE;
    TableView wIndexView =
        new TableView(variables, wGeneral, style, SwtUtil.indexColInfo(), 0, true, lsMod, props);
    wIndexView.setSortable(false);
    wIndex = wIndexView.getTable();
    wIndex.setToolTipText(i18n("CTL.ToolTip.Index"));
    wIndexView.setLayoutData(LData.byTop(wLabel, 0.5));

    wLabel =
        Widgets.label(SWT.NONE)
            .text(i18n("CTL.Label.Indexes"))
            .layoutData(LData.byTop(wIndexView, false))
            .create(wGeneral);
    ColumnInfo[] cols = SwtUtil.tableColInfo();
    style = SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE;
    wRelationTableView =
        new TableView(variables, wGeneral, style, cols, 1, false, this::editRelation, props);
    //    wRelationTableView.setLayoutData(LData.fill(wLabel, null));
    wRelationTableView.setSortable(false);
    wRelationTable = wRelationTableView.getTable();
    wRelationTable.setToolTipText(i18n("CTL.ToolTip.Table"));
    wTableNames = cols[0];

    //// JoinEditor
    int sqlStyle = SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL;
    int sqlReadonlyStyle = sqlStyle | SWT.READ_ONLY;

    wJoinSql = new StyledTextComp(variables, wJoinEditor, sqlStyle);
    wJoinSql.getTextWidget().setToolTipText(i18n("CTL.ToolTip.JoinSql"));
    wJoinSql.setLayoutData(LData.byTop(null, 0.3));
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.JoinSQL"))
        .layoutData(LData.onTop(wJoinSql))
        .create(wJoinEditor);

    wDesignSql = new StyledTextComp(variables, wJoinEditor, sqlStyle | SWT.READ_ONLY);
    wDesignSql.getTextWidget().setToolTipText(i18n("CTL.ToolTip.PreviewSql"));
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.PreviewSql"))
        .layoutData(LData.onTop(wDesignSql))
        .create(wJoinEditor);

    wOrderBy = new StyledTextComp(variables, wJoinEditor, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wOrderBy.setToolTipText(i18n("CTL.ToolTip.OrderBy"));
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.OrderBy"))
        .layoutData(LData.on(wOrderBy))
        .create(wJoinEditor);

    wJoinLimit =
        Widgets.spinner(SWT.BORDER | SWT.WRAP)
            .tooltip(i18n("CTL.ToolTip.JoinLimit"))
            .bounds(1, 255)
            .increment(5, 20)
            .onModify(this::modifyJoinLimit)
            .create(wJoinEditor);
    wJoinLimit.setSelection(1);
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.JoinLimit"))
        .layoutData(LData.on(wJoinLimit))
        .create(wJoinEditor);

    wIgnoreDel =
        Widgets.button(SWT.CHECK)
            .text(i18n("CTL.Label.IgnoreDel"))
            .tooltip(i18n("CTL.Tooltip.IgnoreDel"))
            .layoutData(LData.byBottom(null, false))
            .create(wJoinEditor);

    wJoinLimit.setLayoutData(LData.byBottom(wIgnoreDel, 100 - middle));
    wOrderBy.setLayoutData(LData.byBottom(wJoinLimit));
    //    wDesignSql.setLayoutData(LData.fill(wJoinSql, wOrderBy));

    //// TestData
    wConditionSql = new StyledTextComp(variables, wTestData, sqlStyle);
    wConditionSql.getTextWidget().setToolTipText(i18n("CTL.ToolTip.ConditionSql"));
    wConditionSql.setLayoutData(LData.byTop(null, 0.3));
    Widgets.label(SWT.NONE)
        .text(i18n("CTL.Label.ConditionSql"))
        .layoutData(LData.onTop(wConditionSql))
        .create(wTestData);

    wLoadingSql = new StyledTextComp(variables, wTestData, sqlReadonlyStyle);
    wLoadingSql.getTextWidget().setToolTipText(i18n("CTL.ToolTip.PreviewSql"));
    Widgets.label(SWT.NONE)
        .text(i18n("CTL.Label.PreviewSql"))
        .layoutData(LData.onTop(wLoadingSql))
        .create(wTestData);

    wRowLimit =
        Widgets.spinner(SWT.BORDER | SWT.WRAP | SWT.RIGHT)
            .bounds(1, 1000)
            .increment(10, 100)
            .tooltip(i18n("CTL.ToolTip.RowLimit"))
            .create(wTestData);
    wRowLimit.setSelection(100);
    Widgets.label(SWT.RIGHT)
        .text(i18n("CTL.Label.RowLimit"))
        .layoutData(LData.on(wRowLimit))
        .create(wTestData);

    wShowOriginKey =
        Widgets.button(SWT.CHECK)
            .text(i18n("CTL.Label.ShowOriginKey"))
            .tooltip(i18n("CTL.ToolTip.ShowOriginKey"))
            .layoutData(LData.byBottom(null, false))
            .create(wTestData);
    wRowLimit.setLayoutData(LData.byBottom(wShowOriginKey, 100 - middle));
    //    wLoadingSql.setLayoutData(LData.fill(wConditionSql, wRowLimit));

    setShellImage(shell, input);
    super.wOk =
        Widgets.button(SWT.PUSH).text(i18n("System.Button.OK")).onSelect(this::onOk).create(shell);
    wRefresh =
        Widgets.button(SWT.PUSH)
            .text(i18n("CTL.Button.Refresh"))
            .onSelect(this::onRefresh)
            .create(shell);
    super.wPreview =
        Widgets.button(SWT.PUSH)
            .text(i18n("System.Button.Preview"))
            .onSelect(this::onPreview)
            .create(shell);
    super.wCancel =
        Widgets.button(SWT.PUSH)
            .text(i18n("System.Button.Cancel"))
            .onSelect(this::onCancel)
            .create(shell);

    wTabFolder.setLayoutData(LData.form().left(0).top(wIgnoreNonMap).right(100).bottom(wOk).get());
    positionBottomButtons(
        shell, new Button[] {wOk, wRefresh, wPreview, wCancel}, PropsUi.getMargin(), null);

    SelectionListener listener = Adapter.widgetSelected(e -> input.setChanged());
    wTransformName.addModifyListener(lsMod);
    wConnection.getComboWidget().addModifyListener(lsMod);
    wSchema.getTextWidget().addModifyListener(lsMod);
    wMasterTable.getTextWidget().addModifyListener(lsMod);
    wIgnoreDel.addSelectionListener(listener);
    wIgnoreNonMap.addSelectionListener(listener);
    wRelationTableView.addModifyListener(lsMod);
    wIndexView.addModifyListener(lsMod);
    wFieldView.addModifyListener(lsMod);
    wJoinSql.getTextWidget().addModifyListener(lsMod);
    wOrderBy.getTextWidget().addModifyListener(lsMod);
    wJoinLimit.addSelectionListener(listener);
    wIgnoreDel.addSelectionListener(listener);
    wConditionSql.getTextWidget().addModifyListener(lsMod);
    wRowLimit.addSelectionListener(listener);
    wShowOriginKey.addSelectionListener(listener);

    wRelationTableView.addKeyListener(Adapter.keyPressed(this::searchTable));
    wTabFolder.addSelectionListener(Adapter.widgetSelected(this::switchTabFolder));

    SwtDialog.preferredShellStyle(shell, wOk);
  }

  private String i18n(String key, Object... params) {
    return Util.i18n(SubOpEventMeta.class, key, params);
  }

  private Optional<Relationship> getRef() {
    TableItem item = wRelationTableView.getActiveTableItem();
    if (item == null && wRelationTable.getSelectionIndex() != -1) {
      item = wRelationTable.getItem(wRelationTable.getSelectionIndex());
    }
    if (item != null) {
      return Optional.ofNullable((Relationship) item.getData());
    }
    return Optional.empty();
  }

  @Override
  public String open() {
    PropsUi.setLook(shell);
    PropsUi.setLook(wIndex, PropsUi.WIDGET_STYLE_TABLE);
    PropsUi.setLook(wField, PropsUi.WIDGET_STYLE_TABLE);

    wSchema.addModifyListener(this::modifyMasterTable);
    wMasterTable.addModifyListener(this::modifyMasterTable);
    wKeyFields.addModifyListener(this::modifyMasterTable);
    SelectionListener defSelListener = Adapter.widgetSelected(this::onChanged);
    wIgnoreNonMap.addSelectionListener(defSelListener);
    wIgnoreAllDelOp.addSelectionListener(defSelListener);
    wIgnoreAllDelOp.addSelectionListener(Adapter.widgetSelected(this::onIgnoreAllDelOp));

    wJoinSql.addModifyListener(this::modifyJoinSql);
    wConditionSql.addModifyListener(this::modifyTestSql);
    wConditionSql.addModifyListener(
        e -> getRef().ifPresent(ref -> wLoadingSql.setText(ref.getTestSqlText())));

    wRelationTable.addSelectionListener(Adapter.widgetSelected(this::onSelectRelation));
    wIndex.addSelectionListener(Adapter.widgetSelected(this::selectColumn));
    wField.addSelectionListener(Adapter.widgetSelected(this::checkChanged));
    wJoinLimit.addModifyListener(this::modifyJoinLimit);
    wIgnoreDel.addSelectionListener(Adapter.widgetSelected(this::modifyIgnoreDel));

    //    wTable.addModifyListener(this::joinSqlChanged);
    //    wDesignSql.addModifyListener(this::joinSqlChanged);

    //
    wKeyFields.notifyListeners(SWT.Modify, SwtUtil.newEvent(wKeyFields));
    if (wRelationTable.getItemCount() > 0) {
      wRelationTable.setSelection(0);
      wRelationTable.notifyListeners(
          SWT.Selection, SwtUtil.newEvent(wRelationTable, SwtUtil::tableEvent));
    }

    getData();
    input.setChanged(changed);

    BaseDialog.defaultShellHandling(shell, e -> onOk(null), e -> onCancel(null));
    return transformName;
  }

  private void modifyJoinLimit(ModifyEvent event) {
    int val = ((Spinner) event.widget).getSelection();
    getRef().ifPresent(relationship -> relationship.setLimit(val));
  }

  void searchTable(KeyEvent event) {
    int cr = SWT.CR;
    int keypad_cr = SWT.KEYPAD_CR;
    if ((event.stateMask & SWT.CONTROL) != 0
        && (event.keyCode == cr || event.keyCode == keypad_cr)
        && wRelationTableView.getEditor() != null) {}
  }

  private void modifyIgnoreDel(SelectionEvent event) {
    Button btn = (Button) event.widget;
    getRef().ifPresent(relationship -> relationship.setIgnoreDel(btn.getSelection()));
  }

  private void modifyMasterTable(ModifyEvent event) {
    master.setSchema(wSchema.getText());
    master.setName(wMasterTable.getText());
    boolean mockFields = master.getFields().stream().allMatch(this::isMockField);
    if (mockFields || master.getFields().isEmpty()) {
      master.getFields().clear();
      if (!StringUtil.isEmpty(wKeyFields.getText())) {
        for (String v : wKeyFields.getText().split(",")) {
          if (!StringUtil.isEmpty(v)) {
            master.getFields().add(new ValueMetaBase(v.trim(), IValueMeta.TYPE_NONE));
          }
        }
      }
    }
    master.setPkFieldNames(wKeyFields.getText());
  }

  private boolean isMockField(IValueMeta valueMeta) {
    return valueMeta.getType() == IValueMeta.TYPE_NONE;
  }

  private void checkChanged(SelectionEvent event) {
    if (event.detail != SWT.CHECK) {
      return;
    }
    if (wIndex.getSelectionIndex() != -1) {
      wIndex.setSelection(-1);
    }
    TableItem item = (TableItem) event.item;
    String pkFields = getCheckedColumnNames();
    getRef()
        .ifPresent(
            relationship -> {
              relationship.getDetail().setPkFieldNames(pkFields);
              fillRelation(
                  wRelationTable.getItem(wRelationTable.getSelectionIndex()), relationship);
              wRelationTableView.optWidth(true);
              if (wRelationTable.getSelectionIndex() != -1) {
                wRelationTable.notifyListeners(
                    SWT.Selection,
                    SwtUtil.newEvent(
                        wRelationTable, e -> e.item = wRelationTable.getSelection()[0]));
              }
            });
  }

  private void editRelation(ModifyEvent event) {
    CCombo cCombo = (CCombo) event.widget;
    if (StringUtil.isEmpty(cCombo.getText())) {
      return;
    }
    boolean exist = false;
    String table = cCombo.getText();
    for (String item : cCombo.getItems()) {
      if (item.equalsIgnoreCase(table)) {
        exist = true;
        break;
      }
    }
    if (!exist) {
      return;
    }
    TableItem item = wRelationTable.getItem(wRelationTable.getSelectionIndex());
    if (item.getData() == null) {
      item.setData(
          new Relationship(master, Tab.build(master.getSchema(), table, IValueMeta::getName)));
    }
    Relationship relationship = (Relationship) item.getData();
    if (!table.equalsIgnoreCase(relationship.getDetail().getName())) {
      relationship.getDetail().setName(table);
    }
    wRelationTable.notifyListeners(
        SWT.Selection, SwtUtil.newEvent(wRelationTable, e -> e.item = item));
  }

  private void switchTabFolder(SelectionEvent event) {
    assert event.widget instanceof CTabFolder;
    int selIndex = ((CTabFolder) event.widget).getSelectionIndex();
    wRefresh.setEnabled(selIndex == 0);
    if (selIndex == 2) {
      wConditionSql.notifyListeners(SWT.Modify, SwtUtil.newEvent(wConditionSql));
    } else if (selIndex == 1) {
      wJoinSql.notifyListeners(SWT.Modify, SwtUtil.newEvent(wJoinSql));
    } else if (selIndex == 0) {
      //      wPreviewSql.setText(
      //          String.format("%s\n-- %s", wDesignSql.getText(), wConditionSql.getText()));
    }
  }

  private void modifyJoinSql(ModifyEvent event) {
    Text wText = (Text) event.widget;
    getRef()
        .ifPresent(
            relationship -> {
              relationship.setJoinSql(wText.getText());
              wDesignSql.setText(relationship.getDesignSqlText());
            });
    //    if (wRelationTables.getActiveTableItem() == null) {
    //      return;
    //    }
    //    String detail = wRelationTables.getActiveTableItem().getText(1);
    //    wPreviewSql.setText(re
    //        SubOpEventMeta.getTemplateSql(
    //            wTable.getText(),
    //            wKeyFields.getText(),
    //            detail,
    //            getCheckedColumnNames(),
    //            wDesignSql.getText()));
  }

  private void modifyTestSql(ModifyEvent event) {
    Text wText = (Text) event.widget;
    getRef().ifPresent(relationship -> relationship.setConditionSql(wText.getText()));
    //    String sql = wDesignSql.getText();
    //    if (StringUtil.isEmpty(wText.getText())) {
    //      wTestSql.setText(sql);
    //    } else {
    //      int pos = sql.indexOf("-- WHERE");
    //      if (pos != -1) {
    //        wTestSql.setText(String.format("%sWHERE %s", sql.substring(0, pos), wText.getText()));
    //      }
    //    }

    //    if (wRelationTables.getActiveTableItem() == null) {
    //      return;
    //    }
    //    String detail = wRelationTables.getActiveTableItem().getText(1);
    //    wPreviewSql.setText(
    //        SubOpEventMeta.getTemplateSql(
    //            wTable.getText(),
    //            wKeyFields.getText(),
    //            detail,
    //            getCheckedColumnNames(),
    //            wDesignSql.getText()));
  }

  private void onSaveJoinInfo(SelectionEvent event) {
    if (wRelationTable.getSelectionIndex() == -1) {
      return;
    }
    TableItem item = wRelationTable.getItem(wRelationTable.getSelectionIndex());
    Relationship relation = (Relationship) item.getData();
    //    relation
    //        .getDetail()
    //        .setKeys(
    //            Arrays.stream(wFields.getTable().getItems())
    //                .filter(TableItem::getChecked)
    //                .map(item1 -> (IValueMeta) item1.getData())
    //                .collect(Collectors.toList()));
  }

  private void selectColumn(SelectionEvent event) {
    if (!(event.item instanceof TableItem)) {
      return;
    }
    TableItem indexItem = (TableItem) event.item;
    TableItem[] items = wField.getItems();
    Optional<Relationship> optional = getRef();
    if (!optional.isPresent()) {
      return;
    }
    Relationship relationship = optional.get();
    String pkFields = indexItem.getText(4);
    autoSelectColumn(items, pkFields.split(","));
    Tab<IValueMeta> detail = relationship.getDetail();
    //              detail
    //                  .setKeys(
    //                      Arrays.stream(checkedItems)
    //                          .map(item -> (IValueMeta)
    // item.getData("model.field"))
    //                          .collect(Collectors.toList()));
    detail.setPkFieldNames(pkFields);
    int index = wRelationTable.getSelectionIndex();
    if (index != -1 && !pkFields.equalsIgnoreCase(wRelationTable.getItem(index).getText(5))) {
      fillRelation(wRelationTable.getItem(index), relationship);
    }

    //      if (ref.getKeyFields() != null) {
    //      }
    //      wRelationTables.getTable().redraw();
    if (wRelationTable.getSelectionIndices().length > 0) {
      //        fillRelation(wRelationTables.getTable().getSelection()[0], ref);
    }
    //      wTable.optWidth(true);
  }

  private TableItem[] autoSelectColumn(TableItem[] items, String[] selNames) {
    List<TableItem> list = new ArrayList<>(selNames.length);
    for (TableItem item : items) {
      if (item.getChecked()) {
        item.setChecked(false);
      }
    }

    for (String selName : selNames) {
      String name = selName.trim();
      if (StringUtil.isEmpty(name)) {
        continue;
      }
      for (TableItem item : items) {
        if (!item.getChecked() && name.equalsIgnoreCase(item.getText(1))) {
          item.setChecked(true);
          list.add(item);
        }
      }
    }

    //    String names = getCheckedColumnNames();
    return list.toArray(new TableItem[0]);
  }

  private void jumpToJoinEditor(MouseEvent event) {
    Table view = (Table) event.widget;
    if (view.getSelectionCount() == 0) {
      return;
    }
    Control ref = view;
    while (ref != null) {
      if (ref instanceof CTabFolder) {
        ((CTabFolder) ref).setSelection(1);
        break;
      }
      ref = ref.getParent();
    }
  }

  private void joinSqlChanged(ModifyEvent event) {
    String table = wMasterTable.getText();
    String keyFields = wKeyFields.getText();
    String subTable = "";
    String subKeyFields = getCheckedColumnNames();
    String sqlPattern = "SELECT %s\nFROM %s m\n\tJOIN %s d ON %s...\n--WHERE %s";
    if (wRelationTable.getSelectionCount() > 0) {
      wDesignSql.setText(
          getJoinSql((Relationship) wRelationTableView.getActiveTableItem().getData()));
    }
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

  private boolean hasChanged(TableItem item) {
    Object dataRef = item.getData("model.ref");
    if (dataRef != null && getRef().isPresent()) {
      return latestItem == null || !dataRef.equals(getRef().get());
    }
    return true;
  }

  private void onSelectRelation(SelectionEvent event) {
    logRowlevel(i18n("MSG.SelectRelationInTableView", event.item));
    TableItem item = (TableItem) event.item;
    Optional<Relationship> optional = getRef();
    if (!optional.isPresent() || item == null) {
      return;
    }
    if (StringUtil.isEmpty(item.getText(1))) {
      return;
    }
    logDetailed(i18n("MSG.SelectRelation", item.getText(1)));
    //    if (hasChanged(item)) {
    //      if (latestItem != null) {
    //        String before = detail.get().getPkFieldNames();
    //        detail.get().setPkFieldNames(getCheckedColumnNames());
    //        if (!before.equals(detail.get().getPkFieldNames())) {
    //          fillRelation(latestItem, ref.get());
    //        }
    //      }
    //    }
    Relationship relationship = optional.get();
    Tab<IValueMeta> detail = relationship.getDetail();

    wIndex.setData("model.count", detail.getCardinality());
    SwtUtil.fillTableView(wField, detail.getFields(), SwtUtil::fillField);
    SwtUtil.fillTableView(wIndex, detail.getIndexes(), SwtUtil::fillIndex);
    for (TableItem itemField : wField.getItems()) {
      if (itemField.getChecked()) {
        itemField.setChecked(false);
      }
    }
    if (detail.getPK().isPresent()) {
      if (detail.getPkIndex() != -1) {
        wIndex.setSelection(new int[] {detail.getPkIndex()});
        wIndex.notifyListeners(SWT.Selection, SwtUtil.newEvent(wIndex, wIndex.getSelection()[0]));
      } else {
        autoSelectColumn(wField.getItems(), detail.getPkFieldNames().split(","));
      }
    }
    //    String sql = relationship.getDesignSql();
    //    if (StringUtil.isEmpty(sql)) {
    //      sql = relation.getDesignSql(wIndex.getText(), relation.getTableName(),
    // wKeyFields.getText());
    //    }
    //    wDesignSql.setText(sql);
    //    if (relation == null || relation.getDetail() != null) {
    //      return;
    //      //      OpEventInputDialog.fillTableViewer(wIndexes.getTable(),
    //      // relation.getDetail().getIndexes(), OpEventInputDialog::fillIndex);
    //    }
    wJoinLimit.setSelection(relationship.getLimit());
    wIgnoreDel.setSelection(relationship.isIgnoreDel());
    wDesignSql.setText(relationship.getDesignSqlText());
    //    wTestSql.setText(relationship.getTestSqlText());
    wJoinSql.setText(Const.nullToEmpty(relationship.getJoinSql()));
    wJoinSql.notifyListeners(SWT.Modify, SwtUtil.newEvent(wJoinSql));
    wConditionSql.setText(Const.nullToEmpty(relationship.getConditionSql()));
    wConditionSql.notifyListeners(SWT.Modify, SwtUtil.newEvent(wConditionSql));
    //    wPreviewJoinSql.setText(relationship.getPreviewSql());
    //    wPreviewSql.setText(relationship.getDesignSql());
    //    wTestSql.setText(relationship.getTestSql());
  }

  private String getJoinSql(Relationship relation) {
    StringBuilder builder =
        new StringBuilder(1024)
            .append("SELECT ")
            .append(
                getData(IValueMeta.class, wKeyFields.getData())
                    .map(IValueMeta::getName)
                    .map(s -> "m." + s)
                    .collect(Collectors.joining(", ")))
            .append("\nFROM ");
    if (!StringUtil.isEmpty(wSchema.getText())) {
      builder.append(wSchema.getText()).append(".");
    }
    builder
        .append(wMasterTable.getText())
        .append(" m\n  JOIN ")
        .append(relation.getDetail().getName())
        .append(" t ON ")
        .append(wJoinSql.getText().trim());
    builder.append("\nWHERE ");
    builder.append(
        relation.getDetail().getPkFields().stream()
            .map(IValueMeta::getName)
            .map(s -> "t." + s + " = ?")
            .collect(Collectors.joining(" AND ")));
    return builder.toString();
  }

  private <T> Stream<T> getData(Class<T> type, Object val) {
    if (val instanceof Collection) {
      return ((Collection<T>) val).stream();
    }
    return Stream.empty();
  }

  private boolean hasOpEventInput() {
    for (TransformMeta stepMeta : pipelineMeta.getTransforms()) {
      if ("OpEventInput".equals(stepMeta.getTypeId())) {
        return true;
      }
    }
    return false;
  }

  private void getData() {
    logRowlevel(i18n("MSG.LoadingMeta", input.getName()));
    this.master.refresh(input.getMaster(), true);

    wTransformName.setText(Const.nullToEmpty(transformName));
    wConnection.setItems(pipelineMeta.getDatabaseNames());
    wConnection.setText(Const.nullToEmpty(input.getConnection()));
    wSchema.setText(Const.nullToEmpty(master.getSchema()));
    wMasterTable.setText(Const.nullToEmpty(master.getName()));
    wKeyFields.setText(Const.nullToEmpty(master.getPkFieldNames()));
    wIgnoreNonMap.setSelection(input.isIgnoreNonMap());
    wIgnoreAllDelOp.setSelection(input.isIgnoreAllDelOp());

    List<Relationship> list =
        input.getDetails().stream().map(Relationship::clone).collect(Collectors.toList());
    list.forEach(relationship -> relationship.setMaster(master));
    SwtUtil.fillTableView(wRelationTable, list, this::fillRelation);
    wRowLimit.setSelection(input.getRowLimit());
    //    wFetchAllTables.setSelection(meta.isAllTable());

    if (StringUtil.isEmpty(wConnection.getText()) && wConnection.getItemCount() > 0) {
      wConnection.select(0);
    }
    if (wRelationTable.getItemCount() == 0) {
      wRelationTable.setItemCount(1);
    } else {
      wTableNames.setComboValues(
          list.stream()
              .map(relationship -> relationship.getDetail().getName())
              .toArray(String[]::new));
      wRelationTableView.optWidth(true);
      wRelationTable.select(0);
    }
    disableEditMasterIfNeed();
    if (wConnection.getSelectionIndex() != -1) {
      fillTableNameIfNeed(pipelineMeta.getDatabases().get(wConnection.getSelectionIndex()));
    }
    wRelationTable.notifyListeners(
        SWT.Selection, SwtUtil.newEvent(wRelationTable, e -> e.item = wRelationTable.getItem(0)));

    wTransformName.selectAll();
  }

  private void fillTableNameIfNeed(DatabaseMeta databaseMeta) {
    String schema = wSchema.getText();
    Runnable runnable =
        querySchema(
            databaseMeta,
            metadataUtil -> {
              String[] tableNames = metadataUtil.getTable(schema, null).toArray(new String[0]);
              Display.getDefault()
                  .asyncExec(
                      () -> {
                        wTableNames.setComboValues(tableNames);
                        wRelationTableView.optWidth(true);
                      });
            });
    ExecutorUtil.getExecutor().execute(runnable);
  }

  private boolean masterStepAvailable(TransformMeta stepMeta) {
    if (MASTER_STEP_ID.equals(stepMeta.getTypeId())) {
      OpEventInputMeta refMeta = (OpEventInputMeta) stepMeta.getTransform();
      return !StringUtil.isEmpty(refMeta.getTable()) && !refMeta.getKeyFields().isEmpty();
    }
    return false;
  }

  private void disableEditMasterIfNeed() {
    OpEventInputMeta refMeta = null;
    TransformMeta stepMeta = null;
    Optional<TransformMeta> masterStep =
        pipelineMeta.getTransforms().stream().filter(this::masterStepAvailable).findFirst();
    if (!masterStep.isPresent()) {
      return;
    }
    for (TransformMeta sm : pipelineMeta.getTransforms()) {
      if ("OpEventInput".equals(sm.getTypeId())) {
        refMeta = (OpEventInputMeta) sm.getTransform();
        if (!StringUtil.isEmpty(refMeta.getTable()) && !refMeta.getKeyFields().isEmpty()) {
          stepMeta = sm;
          break;
        }
      }
    }
    if (stepMeta != null) {
      wSchema.setEnabled(false);
      wMasterTable.setEnabled(false);
      wKeyFields.setEnabled(false);
      wConnection.setText(Const.nullToEmpty(refMeta.getConnection()));
      master.refresh(refMeta.getTabModel(), true);
      wSchema.setText(Const.nullToEmpty(refMeta.getSchema()));
      wMasterTable.setText(Const.nullToEmpty(refMeta.getTable()));
      wKeyFields.setText(Const.nullToEmpty(refMeta.getPkFields()));
    }
  }

  private void fillRelation(TableItem item, Relationship relation) {
    Tab<IValueMeta> detail = relation.getDetail();
    if (item.getData() == null) {
      item.setData(relation);
    }
    item.setText(1, Const.nullToEmpty(detail.getName()));
    item.setText(2, Const.nullToEmpty(detail.getPkFieldNames()));
    Index<IValueMeta> index = detail.getPK().orElse(null);
    if (index != null) {
      if (index.isUnique()) {
        item.setText(3, "Y");
      }
      item.setText(6, Const.nullToEmpty(index.getName()));
    }
    long count = detail.getCardinality();
    if (count >= 0) {
      item.setText(4, Long.toString(count));
    }
    if (relation.isIgnoreDel()) {
      item.setText(5, "Y");
    }
    if (detail.getType() != Tab.Type.NONE) {
      item.setText(7, Const.nullToEmpty(detail.getType().name()));
    }
  }

  private void applyJdbcExtensionOption(DatabaseMeta meta, String key, String value) {
    meta.getAttributes().put("EXTRA_OPTION_" + meta.getPluginId() + "." + key, value);
  }

  Runnable querySchema(
      DatabaseMeta databaseMeta, FailableConsumer<MetadataUtil, SQLException> consumer) {
    return () -> {
      DatabaseMeta tmp = (DatabaseMeta) databaseMeta.clone();
      applyJdbcExtensionOption(tmp, "connectTimeout", "2000");
      try (Database db = new Database(loggingObject, variables, tmp)) {
        db.connect();
        logRowlevel(i18n("MSG.ConnectDatabase", db.getObjectName()));
        consumer.accept(
            MetadataUtil.getInstance(variables, db.getDatabaseMeta(), db.getConnection()));
        if (log.isRowLevel()) {
          logRowlevel(i18n("MSG.FinishQuery"));
        }
      } catch (SQLException | HopDatabaseException e) {
        logMinimal(i18n("MSG.Error.QuerySchema", tmp.getName()), e);
      }
    };
  }

  void refreshSchema(TableItem item, MetadataUtil util) throws SQLException {
    Relationship relationship = (Relationship) item.getData();
    //    Tab<IValueMeta> detail = relationship.getDetail();
    //    String schema = detail.getSchema();
    //    String table = item.getText(1);
    if (master.getFields().isEmpty()
        || !master.getPK().isPresent()
        || wMasterTable.getText().equalsIgnoreCase(master.getName())
        || wKeyFields.getText().equalsIgnoreCase(master.getPkFieldNames())
        || master.getPkFields().stream().allMatch(this::isMockField)) {
      master.setSchema(wSchema.getText());
      master.setName(wMasterTable.getText());
      Util.refreshSchema(master, util);
      master.setPkFieldNames(wKeyFields.getText());
    }

    relationship.getDetail().setName(item.getText(1));
    Util.refreshSchema(relationship.getDetail(), util);
    //    detail.setType(Tab.Type
    //    .valueOfValue(util.getTableType(schema, table)));
    //    detail.setFields(util.getRowMeta(schema, table).getValueMetaList());
    //    detail.setIndexes(
    //        util.getIndexes(schema, table, (rowMeta, row) -> SwtUtil.toIndex(detail, rowMeta,
    // row)));
    //    String[] pkNames = util.getPrimaryKeys(schema, table);
    //    if (pkNames.length > 0) {
    //      detail.setPrimaryKeyName(pkNames[0]);
    //      if (!detail.getPrimaryKey().isPresent()) {
    //        detail.setPrimaryKeyFields(pkNames[1]);
    //      }
    //    }
    //    String pkFields = item.getText(5);
    //    if (!StringUtil.isEmpty(pkFields)) {
    //      detail.setPkFieldNames(pkFields);
    //    }
    fillRelation(item, relationship);
    int index = wRelationTable.getSelectionIndex();
    if (index != -1 && item.equals(wRelationTable.getItem(index))) {
      wRelationTable.notifyListeners(
          SWT.Selection, SwtUtil.newEvent(wRelationTable, e -> e.item = item));
    }
  }

  private <T> void onChanged(T event) {
    input.setChanged();
  }

  void onIgnoreAllDelOp(SelectionEvent event) {
    wIgnoreDel.setEnabled(((Button) event.widget).getSelection());
  }

  void onRefresh(SelectionEvent event) {
    logRowlevel(i18n("MSG.RefreshSchema"));
    TableItem[] items = wRelationTable.getSelection();
    if (items.length == 0) {
      return;
    }
    Control self = (Control) event.widget;
    self.setEnabled(false);
    List<TableItem> itemList = new ArrayList<>(items.length);
    for (TableItem item : items) {
      if (item.getData() instanceof Relationship) {
        itemList.add(item);
      }
    }
    DatabaseMeta databaseMeta = pipelineMeta.findDatabase(wConnection.getText(), variables);
    Runnable runnable =
        querySchema(
            databaseMeta,
            metadataUtil -> {
              for (TableItem item : itemList) {
                refreshSchema(item, metadataUtil);
              }
              Display.getDefault().asyncExec(() -> self.setEnabled(true));
            });
    runnable.run();

    //    Display.getDefault()
    //        .asyncExec(
    //            () -> {
    //              try {
    //                runnable.run();
    //              } finally {
    //                self.setEnabled(true);
    //              }
    //            });

    //    Map<String, Tab<IValueMeta>> tableMap;
    //    Database db = new Database(pipelineMeta,
    // pipelineMeta.findDatabase(wConnection.getText()));
    //    try {
    //      db.connect();
    //      logDebug(i18n("MSG.ConnectDatabase", db.getObjectName()));
    //      MetadataUtil util = MetadataUtil.getInstance(db.getDatabaseMeta(), db.getConnection());
    //
    //      logBasic(i18n("MSG.QueryTableNames", wSchema.getText()));
    //      if (wTableNames.getComboValues().length == 0) {
    //        String[] strings = util.getTable(wSchema.getText(), "").toArray(new String[0]);
    //        wTableNames.setComboValues(strings);
    //      }
    //
    //      //      logBasic(i18n("MSG.QueryTableSchema", tables));
    //      //      tableMap = querySchema(util, wSchema.getText(), tables.toArray(new String[0]));
    //    } catch (SQLException | HopDatabaseException e) {
    //      throw new IllegalStateException(e);
    //    } finally {
    //      db.disconnect();
    //      self.setEnabled(true);
    //    }
    //    if (tableMap.containsKey(wMasterTable.getText())) {
    //      master.refresh(tableMap.get(wMasterTable.getText()));
    //    }
    //    for (TableItem item : items) {
    //      String name = item.getText(1);
    //      if (StringUtil.isEmpty(name) || !tableMap.containsKey(name)) {
    //        continue;
    //      }
    //      Tab<IValueMeta> info = tableMap.get(name);
    //      //      item.setData("model.table", info);
    //      //      item.setData("model.key", info.getPkFields());
    //      //      info.getPK().ifPresent(indexInfo -> item.setData("model.pk", indexInfo));
    //      //      Relationship ref = (Relationship) item.getData("model.ref");
    //      //      if (ref == null) {
    //      //        ref = new Relationship(master, info);
    //      //        item.setData("model.ref", ref);
    //      //      }
    //      Relationship currentRef = (Relationship) item.getData("model.ref");
    //      if (currentRef == null) {
    //        currentRef =
    //            new Relationship(master, Tab.build(item.getText(1), IValueMeta::getName));
    //        item.setData("model.ref", currentRef);
    //      }
    //      currentRef.getDetail().refresh(info);
    //      fillRelation(item, currentRef);
    //    }
    //    wTableView.optWidth(true);
    //    if (wTable.getSelectionIndex() != -1) {
    //      //      wTable.notifyListeners(
    //      //          SWT.Selection, SwtUtil.newEvent(wTable,
    //      // wTable.getItems()[wTable.getSelectionIndex()]));
    //    }
  }

  private Map<String, Tab<IValueMeta>> querySchema(
      MetadataUtil util, String schema, String... tableList) {
    Map<String, Tab<IValueMeta>> list = new HashMap<>(tableList.length + 1);
    for (String table : tableList) {
      Tab<IValueMeta> tab = Tab.build(schema, table, IValueMeta::getName);
      try {
        tab.setType(Tab.Type.valueOfValue(util.getTableType(schema, table)));
        tab.setFields(util.getRowMeta(schema, table).getValueMetaList());
        tab.setIndexes(
            util.getIndexes(
                wSchema.getText(), table, (rowMeta, row) -> SwtUtil.toIndex(tab, rowMeta, row)));
        String[] pkNames = util.getPrimaryKeys(schema, table);
        if (pkNames.length > 0) {
          tab.setPrimaryKeyName(pkNames[0]);
          if (!tab.getPrimaryKey().isPresent()) {
            tab.setPrimaryKeyFields(pkNames[1]);
          }
        }
      } catch (Exception ignore) {
        logError("xx", ignore);
      }
      list.put(table, tab);
    }
    return list;
  }

  void onOk(SelectionEvent event) {
    if (StringUtil.isEmpty(wTransformName.getText())) {
      return;
    }
    SubOpEventMeta meta = (SubOpEventMeta) input;
    transformName = wTransformName.getText();
    if (meta.getMaster().getFields().isEmpty() && !master.getFields().isEmpty()) {
      meta.getMaster().refresh(master, true);
    }
    meta.setConnection(wConnection.getText());
    meta.setSchema(wSchema.getText());
    meta.setTable(wMasterTable.getText());
    if (!StringUtil.isEmpty(wKeyFields.getText())) {
      meta.getMaster().setPkFieldNames(wKeyFields.getText());
    }
    meta.setIgnoreNonMap(wIgnoreNonMap.getSelection());
    meta.setIgnoreAllDelOp(wIgnoreAllDelOp.getSelection());
    int nr = wRelationTableView.nrNonEmpty();
    if (nr > 0) {
      List<Relationship> list = new ArrayList<>(nr);
      for (TableItem item : wRelationTableView.getTable().getItems()) {
        if (item.getData() instanceof Relationship) {
          list.add((Relationship) item.getData());
        }
      }
      meta.setDetails(list);
    }

    meta.setChanged();
    dispose();
  }

  void onCancel(SelectionEvent event) {
    transformName = null;
    ((SubOpEventMeta) input).setChanged(backupChanged);
    dispose();
  }

  void onPreview(SelectionEvent event) {
    SubOpEventMeta meta = (SubOpEventMeta) input.clone();
    meta.setSchema(wSchema.getText());
    meta.setTable(wMasterTable.getText());
    meta.getMaster().setPkFieldNames(wKeyFields.getText());
    getRef().ifPresent(relationship -> meta.setDetails(Collections.singletonList(relationship)));
    meta.setRowLimit(wRowLimit.getSelection());
    logDetailed(i18n("MSG.PreviewTableData", wTransformName.getText()));
    //    try {
    String key = SubOpEvent.TRANS_PARAM_KEY;
    variables.setVariable(key, getRef().get().getDetail().getName());
    //      pipelineMeta.addParameterDefinition(key, null, null);
    //      pipelineMeta.setParameterValue(key, getRef().get().getDetail().getName());
    //    } catch (HopException ignore) {
    //    }
    try {
      //      Listeners.preview(
      //          shell, variables, meta, metadataProvider, wRowLimit.getSelection(),
      // transformName);
    } finally {
      variables.setVariable(key, null);
    }
  }

  private void guessMasterTable(SubOpEventMeta meta, ITransformMeta masterStepMeta) {
    try {
      if (StringUtil.isEmpty(meta.getConnection())) {
        BeanUtils.copyProperty(masterStepMeta, "databaseMeta", meta);
      }
      if (isEmpty(meta.getSchema())) {
        BeanUtils.copyProperty(masterStepMeta, "schemaName", meta);
      }
      if (isEmpty(meta.getTable())) {
        BeanUtils.copyProperty(masterStepMeta, "tableName", meta);
      }
      if (meta.getKeyFields().isEmpty()) {
        //                List<IValueMeta> fields = (List<IValueMeta>)
        // ReflectUtil.invokeMethod(
        //                        ReflectUtil.findMethod(masterStepMeta.getClass(), "getKefFields"),
        // masterStepMeta);
        //                if (CollectionUtils.isNotEmpty(fields)) {
        //
        // meta.getPrimaryKey().setColumns(fields.stream().map(IValueMeta::getName).collect(Collectors.toList()));
        //                }
      }
    } catch (Exception ignored) {
    }
  }

  private void selectSchema(SelectionEvent event) {
    Listeners.selectSchema(
            variables, pipelineMeta, wConnection.getComboWidget(), wSchema.getTextWidget())
        .widgetSelected(event);
  }

  private void selectTable(SelectionEvent event) {
    Listeners.selectTable(
            variables,
            pipelineMeta,
            wConnection.getComboWidget(),
            wSchema.getTextWidget(),
            wMasterTable.getTextWidget())
        .widgetSelected(event);
  }

  private Optional<ITransformMeta> detectOpEventStepMeta(PipelineMeta pipelineMeta) {
    return pipelineMeta.getTransforms().stream()
        .filter(this::isOpEventStep)
        .map(TransformMeta::getTransform)
        .findFirst();
  }

  private boolean isOpEventStep(TransformMeta stepMeta) {
    return MASTER_STEP_ID.equals(stepMeta.getTypeId());
  }
}
