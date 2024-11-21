package org.apache.hop.testing.tool;

import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

public class EventViewer extends Dialog {
  final Shell shell;
  private final ToolBar wToolBar;
  private final Combo wI18n;
  private final Tree wPlugins;
  private final Text wSearch;
  private ToolBar wStatus;
  private CoolBar wStatusLine;

  private final ToolItem[] wToolItems;

  public EventViewer(Shell parent) {
    this(parent, SWT.TITLE | SWT.RESIZE | SWT.ON_TOP);
    //    this(parent, SWT.SYSTEM_MODAL | SWT.RESIZE| SWT.ON_TOP);
  }

  public EventViewer(Shell parent, int style) {
    super(parent, style);
    shell = Widgets.shell(style).text("Hop Previewer").layout(Layouts.defaultForm()).create(parent);

    wToolBar = new ToolBar(shell, SWT.WRAP | SWT.FLAT | SWT.HORIZONTAL);
    wToolBar.setLayoutData(LData.form().left(0).right(100).get());
    wToolItems = new ToolItem[6];
    wToolItems[0] = new ToolItem(wToolBar, SWT.PUSH);
    wToolItems[0].setText("Btn...");
    wToolItems[0].setImage(shell.getDisplay().getSystemImage(SWT.ICON_SEARCH));
    wToolItems[0].setHotImage(shell.getDisplay().getSystemImage(SWT.ICON_INFORMATION));
    wToolItems[0].setDisabledImage(shell.getDisplay().getSystemImage(SWT.ICON_WARNING));

    int middle = 35;
    int margin = 9;

    wI18n = new Combo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wI18n.setItems("En_US");
    wI18n.select(0);
    wI18n.setLayoutData(LData.byTop(wToolBar));
    Widgets.label(SWT.RIGHT).text("Language:").layoutData(LData.on(wI18n)).create(shell);

    //    wStatus = new ToolBar(shell, SWT.FLAT | SWT.HORIZONTAL);
    //    wStatus.setLayoutData(LData.form().left(0).right(100).bottom(100).get());
    //    new ToolItem(wStatus, SWT.SEPARATOR);

    wStatusLine = new CoolBar(shell, SWT.HORIZONTAL);
    wStatusLine.setLayoutData(LData.form().left(0).right(100).bottom(100).get());
    new CoolItem(wStatusLine, SWT.SEPARATOR).setText("Rules:");

    wSearch =
        Widgets.text(SWT.SINGLE | SWT.BORDER)
            .layoutData(LData.form().left(middle).right(100).bottom(wStatusLine).get())
            .create(shell);
    Widgets.label(SWT.RIGHT).text("Search:").layoutData(LData.on(wSearch)).create(shell);

    wPlugins =
        Widgets.tree(SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL)
            .layoutData(
                LData.form(90, 280).left(0).top(wI18n).right(100).bottom(wSearch, -margin).get())
            .headerVisible(true)
            .create(shell);

    shell.pack();
    shell.setMinimumSize(shell.getSize());
  }

  public Shell getShell() {
    return shell;
  }
}
