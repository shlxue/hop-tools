package org.apache.hop.example.demo1;

import org.apache.hop.core.Const;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.widget.MetaSelectionLine;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.SwtDialog;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.*;

import java.util.Arrays;

public class Demo1Dialog extends BaseTransformDialog {
  private static Class<?> PKG = Demo1Meta.class;

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
        Widgets.shell(SWT.SHELL_TRIM | SWT.BORDER)
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
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.Name"))
        .layoutData(LData.on(wAge))
        .create(wGeneral);

    wCustomType =
        Widgets.C.combo(SWT.BORDER | SWT.FLAT | SWT.DROP_DOWN)
            .layoutData(LData.byTop(wAge))
            .onModify(this::onChanged)
            .create(wGeneral);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.Name"))
        .layoutData(LData.on(wCustomType))
        .create(wGeneral);

    wContent =
        Widgets.text(SWT.SINGLE | SWT.TRAIL | SWT.BORDER)
            .layoutData(LData.byTop(wCustomType))
            .onModify(this::onChanged)
            .create(wGeneral);
    wContent.setMessage("xxx");
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.Name"))
        .layoutData(LData.on(wContent))
        .create(wGeneral);

    wType =
        Widgets.Ext.combo(SWT.SIMPLE)
            .layoutData(LData.byTop(wContent))
            .onModify(this::onChanged)
            .create(wGeneral);
    Widgets.label(SWT.RIGHT)
        .text(BaseMessages.getString(PKG, "Demo1Dialog.Label.Name"))
        .layoutData(LData.on(wType))
        .create(wGeneral);

    setShellImage(shell, this.input);
    wTabFolder.setLayoutData(LData.fill(wConnection, shell, 120, false));
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

    shell.getDisplay().asyncExec(this::lazyFillFields);
    getData();
    input.setChanged(changed);

    SwtDialog.defaultShellHanding(shell, this::ok, this::cancel);
    return transformName;
  }

  private void lazyFillFields() {
    try {
      IRowMeta rowMeta = pipelineMeta.getPrevTransformFields(variables, transformMeta);
      if (rowMeta != null) {
        String[] fieldNames = rowMeta.getFieldNames();
        Arrays.sort(fieldNames);
        if (!shell.isDisposed() && fieldNames.length > 0) {
          shell.getDisplay().asyncExec(() -> onRefreshFields(fieldNames));
        } else {
          shell.getDisplay().asyncExec(() -> onRefreshFields("f7 f9 f4".split(" ")));
        }
      } else {
        if (!shell.isDisposed()) {
          shell.getDisplay().asyncExec(() -> onRefreshFields("f1 f3 f4".split(" ")));
        }
      }
    } catch (HopTransformException e) {
    }
  }

  private void onRefreshFields(String[] fieldNames) {
    //    wType.setItems(fieldNames);
  }

  private void getData() {
    wTransformName.setText(Const.nullToEmpty(transformName));
    //    wConnection.setText(Const.nullToEmpty(input.getConnection()));
    //    wAge.setSelection(input.getAge());
  }

  private void ok(SelectionEvent event) {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }

    //    input.setConnection(wConnection.getText());
    //    input.setAge(wAge.getSelection());
    this.transformName = wTransformName.getText();
    this.dispose();
  }

  private void cancel(SelectionEvent event) {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private <T> void onChanged(T event) {
    input.setChanged();
  }
}
