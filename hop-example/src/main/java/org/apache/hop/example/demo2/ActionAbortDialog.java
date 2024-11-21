package org.apache.hop.example.demo2;

import org.apache.hop.core.Const;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
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

public class ActionAbortDialog extends ActionDialog {
  private final Text wName;

  private final ActionAbort action;

  public ActionAbortDialog(
      Shell parent, ActionAbort action, WorkflowMeta workflowMeta, IVariables variables) {
    super(parent, workflowMeta, variables);
    this.action = action;

    super.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.RESIZE).layout(Layouts.defaultForm()).create(parent);

    wName =
        Widgets.text(SWT.BORDER).layoutData(LData.byTop(null, true, Const.LENGTH)).create(shell);
    Widgets.label(SWT.RIGHT).text("Name").layoutData(LData.on(wName)).create(shell);

    Button wOk =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.OK"))
            .onSelect(this::ok)
            .create(shell);
    Button wCancel =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.Cancel"))
            .onSelect(this::cancel)
            .create(shell);

    BaseTransformDialog.positionBottomButtons(this.shell, new Button[] {wOk, wCancel}, 0, wName);
    SwtDialog.preferredShellStyle(shell, wOk);
  }

  @Override
  public IAction open() {
    return new ActionAbort();
  }

  private void ok(SelectionEvent event) {
    wName.setText(Const.nullToEmpty(action.getName()));
    action.setName(wName.getText());
    shell.dispose();
  }

  private void cancel(SelectionEvent event) {
    wName.setText(Const.nullToEmpty(action.getName()));
    action.setName(wName.getText());
    shell.dispose();
  }
}
