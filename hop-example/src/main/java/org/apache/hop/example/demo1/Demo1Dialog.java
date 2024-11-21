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
import org.apache.hop.ui.util.SwtDialog;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

public class Demo1Dialog extends BaseTransformDialog {
  private static Class<?> PKG = Demo1Meta.class;

  private MetaSelectionLine<DatabaseMeta> wConnection;
  private final Spinner wAge;

  private final Demo1Meta input;

  public Demo1Dialog(
      Shell parent, IVariables variables, Demo1Meta input, PipelineMeta pipelineMeta) {
    super(parent, variables, input, pipelineMeta);
    this.input = input;
    this.changed = this.input.hasChanged();

    super.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE)
            .text(i18n("Demo1Dialog.DialogTitle"))
            .layout(Layouts.defaultForm())
            .create(parent);

    wTransformName =
        Widgets.text(SWT.BORDER)
            .text(transformName)
            .layoutData(LData.byTop(null))
            .onSelect(this::onChanged)
            .create(shell);
    Widgets.label(SWT.RIGHT)
        .text(i18n("System.Label.TransformName"))
        .layoutData(LData.on(wTransformName))
        .create(shell);

    wConnection =
        addConnectionLine(shell, wTransformName, this.input.getConnection(), this::onChanged);
    ((FormData) wConnection.getLabelWidget().getLayoutData()).left = null;

    wAge =
        Widgets.spinner(SWT.BORDER)
            .layoutData(LData.byTop(wConnection))
            .onModify(this::onChanged)
            .create(shell);
    Widgets.label(SWT.RIGHT)
        .text(i18n("Demo1Dialog.Label.Name"))
        .layoutData(LData.on(wAge))
        .create(shell);

    setShellImage(shell, this.input);
    wOk = Widgets.button(SWT.PUSH).text(i18n("System.Button.OK")).onSelect(this::ok).create(shell);
    wCancel =
        Widgets.button(SWT.PUSH)
            .text(i18n("System.Button.Cancel"))
            .onSelect(this::cancel)
            .create(shell);

    positionBottomButtons(shell, new Button[] {wOk, wCancel}, 0, wAge);
    SwtDialog.preferredShellStyle(shell, wOk);
  }

  @Override
  public String open() {
    PropsUi.setLook(shell);

    if (pipelineMeta != null && pipelineMeta.getDatabaseNames() != null) {
      wConnection.setItems(pipelineMeta.getDatabaseNames());
    }

    getData();
    input.setChanged(changed);

    SwtDialog.defaultShellHanding(shell, this::ok, this::cancel);
    return transformName;
  }

  private void getData() {
    wTransformName.setText(Const.nullToEmpty(transformName));
    wConnection.setText(Const.nullToEmpty(input.getConnection()));
    wAge.setSelection(input.getAge());
  }

  private void ok(SelectionEvent event) {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }

    input.setConnection(wConnection.getText());
    input.setAge(wAge.getSelection());
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

  private String i18n(String key, Object... args) {
    return BaseMessages.getString(PKG, key, args);
  }
}
