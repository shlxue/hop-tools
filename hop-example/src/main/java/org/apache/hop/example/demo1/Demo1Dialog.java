package org.apache.hop.example.demo1;

import org.apache.hop.core.Const;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.widget.MetaSelectionLine;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.AsyncFetch;
import org.apache.hop.ui.util.SwtDialog;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.*;

public class Demo1Dialog extends BaseTransformDialog {
  private static final Class<?> PKG = Demo1Meta.class;

  private final MetaSelectionLine<DatabaseMeta> wConnection;
  private final Spinner wAge;
  private final Text wContent;
  private final Combo wType;
  private final CCombo wCustomType;

  private final Demo1Meta input;

  public Demo1Dialog(
      Shell parent, IVariables variables, Demo1Meta input, PipelineMeta pipelineMet) {
    this(parent, variables, input, pipelineMet, input.getParentTransformMeta().getName());
  }

  public Demo1Dialog(
      Shell parent,
      IVariables variables,
      Demo1Meta input,
      PipelineMeta pipelineMeta,
      String transformName) {
    super(parent, variables, input, pipelineMeta, transformName);
    this.input = input;
    this.changed = this.input.hasChanged();

    super.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE)
            .text(BaseMessages.getString(PKG, "Demo1Dialog.DialogTitle"))
            .layout(Layouts.defaultForm())
            .create(parent);

    wTransformName =
        Widgets.text(SWT.BORDER)
            .text(transformName)
            .layoutData(LData.byTop(null))
            .onSelect(this::onChanged)
            .create(shell);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString("System.Label.TransformName"))
        .layoutData(LData.on(wTransformName))
        .create(shell);

    wConnection =
        addConnectionLine(shell, wTransformName, this.input.getConnection(), this::onChanged);
    ((FormData) wConnection.getLabelWidget().getLayoutData()).left = null;

    CTabFolder wTabFolder = Widgets.C.tabFolder(SWT.BORDER).create(shell);
    Composite wGeneral =
        Widgets.composite(SWT.BORDER).layout(Layouts.nestedForm()).create(wTabFolder);
    Widgets.C.tabItem(SWT.NONE).text("General").control(wGeneral).create(wTabFolder);
    wTabFolder.setSelection(0);

    wAge =
        Widgets.spinner(SWT.BORDER)
            .layoutData(LData.byTop(null))
            .onModify(this::onChanged)
            .create(wGeneral);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.Age"))
        .layoutData(LData.on(wAge))
        .create(wGeneral);

    wCustomType =
        Widgets.C.combo(SWT.BORDER | SWT.FLAT | SWT.DROP_DOWN)
            .layoutData(LData.byTop(wAge))
            .onModify(this::onChanged)
            .create(wGeneral);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.CustomType"))
        .layoutData(LData.on(wCustomType))
        .create(wGeneral);

    wContent =
        Widgets.text(SWT.SINGLE | SWT.TRAIL | SWT.BORDER)
            .layoutData(LData.byTop(wCustomType))
            .message("Content text")
            .onModify(this::onChanged)
            .create(wGeneral);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.Content"))
        .layoutData(LData.on(wContent))
        .create(wGeneral);

    wType =
        Widgets.Ext.combo(SWT.SIMPLE)
            .layoutData(LData.byTop(wContent))
            .onModify(this::onChanged)
            .create(wGeneral);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.Type"))
        .layoutData(LData.on(wType))
        .create(wGeneral);

    setShellImage(shell, this.input);
    wTabFolder.setLayoutData(LData.fill(wConnection, shell, -1, false));
    wOk =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.OK"))
            .onSelect(this::ok)
            .create(shell);
    wCancel =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.Cancel"))
            .onSelect(this::cancel)
            .create(shell);

    positionBottomButtons(shell, new Button[] {wOk, wCancel}, 0, wTabFolder);
    SwtDialog.preferredShellStyle(shell, wOk);
  }

  @Override
  public String open() {
    PropsUi.setLook(shell);
    shell.layout(true, true);

    getData();
    input.setChanged(changed);
    AsyncFetch.of(shell.getDisplay())
        .prevTransformFieldNames(variables, pipelineMeta, transformName, this::onRefreshFields);

    SwtDialog.defaultShellHanding(shell, this::ok, this::cancel);
    return transformName;
  }

  void onRefreshFields(String[] fieldNames) {
    wType.setItems(fieldNames);
  }

  private void getData() {
    wConnection.setText(Const.nullToEmpty(input.getConnection()));
    wAge.setSelection(input.getAge());
    wContent.setText(Const.nullToEmpty(input.getContent()));
    wType.select(input.getType());
    wCustomType.select(input.getCustomType());
  }

  private void ok(SelectionEvent event) {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }
    input.setConnection(wConnection.getText());
    input.setAge(wAge.getSelection());
    input.setContent(wContent.getText());
    input.setType(wType.getSelectionIndex());
    input.setCustomType(wCustomType.getSelectionIndex());
    this.transformName = wTransformName.getText();
    this.dispose();
  }

  private void cancel(SelectionEvent event) {
    transformName = null;
    input.setChanged(changed);
    shell.dispose();
  }

  <T> void onChanged(T event) {
    input.setChanged();
  }
}
