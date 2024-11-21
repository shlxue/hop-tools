package org.apache.hop.example.demo2;

import org.apache.hop.core.Const;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.util.SwtDialog;
import org.apache.hop.ui.widgets.Adapter;
import org.apache.hop.ui.widgets.Widgets;
import org.apache.hop.ui.workflow.action.ActionDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.List;

public class ActionAbortDialog extends ActionDialog {
  private final Text wName;

  private final ActionAbort action;

  public Shell getShell() {
    return shell;
  }

  public ActionAbortDialog(
      Shell parent, ActionAbort action, WorkflowMeta workflowMeta, IVariables variables) {
    super(parent, workflowMeta, variables);
    this.action = action;

    super.shell =
        Widgets.shell(SWT.DIALOG_TRIM | SWT.RESIZE).layout(Layouts.defaultForm()).create(parent);

    wName =
        Widgets.text(SWT.BORDER).layoutData(LData.byTop(null, true, Const.LENGTH)).create(shell);
    Widgets.label(SWT.RIGHT).text("Name").layoutData(LData.on(wName)).create(shell);

    wName.addFocusListener(Adapter.focusGained(this::onFocus));

    CTabFolder wTabFolder = Widgets.C.tabFolder(SWT.BORDER).create(shell);
    FormLayout layout = new FormLayout();
    layout.spacing = 8;
    Composite wGeneral =
        Widgets.composite(SWT.NONE)
            .layout(Layouts.nestedForm())
            .layoutData(LData.form().left(0).top(wName).right(100).bottom(100).get())
            .create(wTabFolder);
    Widgets.C.tabItem(SWT.NONE).text("General").control(wGeneral).create(wTabFolder);
    wTabFolder.setSelection(0);

    //    TabFolder wTabFolder = new TabFolder(shell, SWT.BORDER);
    //    Composite wGeneral =
    // Widgets.composite(SWT.NONE).layout(Layouts.nestedForm()).create(wTabFolder);
    //    TabItem tabItem =
    //    new TabItem(wTabFolder, SWT.NONE);
    //    tabItem.setControl(wGeneral);
    //    tabItem.setText("General");
    //    wTabFolder.setSelection(0);

    Text wText = Widgets.text(SWT.BORDER).layoutData(LData.byTop(null)).create(wGeneral);
    Widgets.label(SWT.RIGHT).text("Name").layoutData(LData.on(wText)).create(wGeneral);
    wText.addFocusListener(Adapter.focusGained(this::onFocus));

    shell.addControlListener(
        Adapter.controlResized(
            event -> {
              //      wGeneral.pack(true);
              Event e = new Event();
              e.widget = wName;
              onFocus(new FocusEvent(e));
              e.widget = wText;
              onFocus(new FocusEvent(e));
            }));
    Button wOk =
        Widgets.button(SWT.PUSH)
            .text(BaseMessages.getString("System.Button.OK"))
            .layoutData(LData.form().left(35).bottom(100).get())
            .onSelect(this::ok)
            .create(shell);
    //    Button wCancel =
    //        Widgets.button(SWT.PUSH)
    //            .text(BaseMessages.getString("System.Button.Cancel"))
    //            .onSelect(this::cancel)
    //            .create(shell);
    wTabFolder.setLayoutData(LData.form().left(0).top(wName).right(100).bottom(wOk).get());

    //    BaseTransformDialog.positionBottomButtons(this.shell, new Button[] {wOk}, 0, wTabFolder);
    SwtDialog.fixNestedFormOffset(shell);
    SwtDialog.preferredShellStyle(shell, wOk);
  }

  private void onFocus(FocusEvent e) {
    Control widget = (Control) e.widget;
    if (widget instanceof Text wT) {
      wT.setText(String.format("%s: %s", wT.getLocation(), depthStr(widget)));
    }
  }

  private String depthStr(Control control) {
    List<String> list = new ArrayList<>(4);
    while (control != null) {
      control = control.getParent();
      if (!(control instanceof Shell)) {
        break;
      }
      list.add(String.format("%d", control.getLocation().x));
    }
    return String.join("; ", list);
  }

  private int depth(Control control) {
    int offset = 0;
    while (!(control instanceof Shell)) {
      offset += 8;
      control = control.getParent();
    }
    return offset;
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
