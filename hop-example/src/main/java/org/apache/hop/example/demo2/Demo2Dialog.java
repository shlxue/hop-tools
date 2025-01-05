package org.apache.hop.example.demo2;

import org.apache.hop.core.Const;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.SwtDialog;
import org.apache.hop.ui.widgets.Widgets;
import org.apache.hop.ui.workflow.action.ActionDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Demo2Dialog extends ActionDialog {
  private final Text wName;

  private final Demo2 action;
  private final boolean changed;

  public Demo2Dialog(Shell parent, Demo2 action, WorkflowMeta workflowMeta, IVariables variables) {
    super(parent, workflowMeta, variables);
    this.action = action;
    this.changed = action.hasChanged();

    super.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE)
            .layout(Layouts.defaultForm())
            .create(parent);

    wName =
        Widgets.text(SWT.BORDER)
            .layoutData(LData.byTop(null, true, Const.LENGTH))
            .onModify(this::onChanged)
            .create(shell);
    Widgets.label(SWT.RIGHT).text("Name").layoutData(LData.on(wName)).create(shell);

    Button wOk =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.OK"))
            .layoutData(LData.form().left(35).bottom(100).get())
            .onSelect(this::ok)
            .create(shell);
    Button wCancel =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.Cancel"))
            .onSelect(this::cancel)
            .create(shell);

    BaseTransformDialog.positionBottomButtons(this.shell, new Button[] {wOk, wCancel}, 0, wName);
    SwtDialog.fixNestedFormOffset(shell);
    SwtDialog.preferredShellStyle(shell, wOk);
  }

  @Override
  public IAction open() {
    PropsUi.setLook(shell);
    shell.layout(true);

    getData();
    action.setChanged(changed);

    SwtDialog.defaultShellHanding(shell, this::ok, this::cancel);
    return action;
  }

  private void getData() {
    wName.setText(Const.nullToEmpty(action.getName()));
  }

  private void ok(SelectionEvent event) {
    action.setName(wName.getText());
    dispose();
  }

  private void cancel(SelectionEvent event) {
    action.setChanged(changed);
    action.setName(null);
    shell.dispose();
  }

  <T> void onChanged(T event) {
    action.setChanged();
  }
}
