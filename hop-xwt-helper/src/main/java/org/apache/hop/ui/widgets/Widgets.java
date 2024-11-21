package org.apache.hop.ui.widgets;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.*;

import java.util.function.Function;

public final class Widgets {
  private Widgets() {}

  public static <P extends Composite, W extends Control> MixedVarPanelFactory<P, W> ofVar(
      P parent, Function<P, W> getter) {
    return MixedVarPanelFactory.of(parent, getter);
  }

  public static ButtonFactory of(Button button) {
    return ButtonFactory.of(button);
  }

  public static TextFactory of(Text control) {
    return TextFactory.of(control);
  }

  public static LabelFactory of(Label control) {
    return LabelFactory.of(control);
  }

  public static CompositeFactory of(Composite control) {
    return CompositeFactory.of(control);
  }

  public static DateTimeFactory of(DateTime control) {
    return DateTimeFactory.of(control);
  }

  public static SpinnerFactory of(Spinner control) {
    return SpinnerFactory.of(control);
  }

  public static TableFactory of(Table control) {
    return TableFactory.of(control);
  }

  public static TreeFactory of(Tree control) {
    return TreeFactory.of(control);
  }

  public static TableColumnFactory of(TableColumn control) {
    return TableColumnFactory.of(control);
  }

  public static TreeColumnFactory of(TreeColumn control) {
    return TreeColumnFactory.of(control);
  }

  public static SashFactory of(Sash control) {
    return SashFactory.of(control);
  }

  public static SashFormFactory of(SashForm control) {
    return SashFormFactory.of(control);
  }

  public static ShellFactory of(Shell control) {
    return ShellFactory.of(control);
  }

  public static GroupFactory of(Group control) {
    return GroupFactory.of(control);
  }

  public static BrowserFactory of(Browser control) {
    return BrowserFactory.of(control);
  }

  public static ButtonFactory button(int style) {
    return ButtonFactory.newButton(style);
  }

  public static TextFactory text(int style) {
    return TextFactory.newText(style);
  }

  public static LabelFactory label(int style) {
    return LabelFactory.newLabel(style);
  }

  public static CompositeFactory composite(int style) {
    return CompositeFactory.newComposite(style);
  }

  public static DateTimeFactory dateTime(int style) {
    return DateTimeFactory.newDateTime(style);
  }

  public static SpinnerFactory spinner(int style) {
    return SpinnerFactory.newSpinner(style);
  }

  public static TableFactory table(int style) {
    return TableFactory.newTable(style);
  }

  public static TreeFactory tree(int style) {
    return TreeFactory.newTree(style);
  }

  public static TableColumnFactory tableColumn(int style) {
    return TableColumnFactory.newTableColumn(style);
  }

  public static TreeColumnFactory treeColumn(int style) {
    return TreeColumnFactory.newTreeColumn(style);
  }

  public static SashFactory sash(int style) {
    return SashFactory.newSash(style);
  }

  public static SashFormFactory sashForm(int style) {
    return SashFormFactory.newSashForm(style);
  }

  public static ShellFactory shell(int style) {
    return ShellFactory.newShell(style);
  }

  public static GroupFactory group(int style) {
    return GroupFactory.newGroup(style);
  }

  public static BrowserFactory browser(int style) {
    return BrowserFactory.newBrowser(style);
  }

  public static final class Ext {
    private Ext() {}

    public static ComboFactory of(Combo combo) {
      return ComboFactory.of(combo);
    }

    public static ToolBarFactory of(ToolBar control) {
      return ToolBarFactory.of(control);
    }

    public static ToolItemFactory of(ToolItem control) {
      return ToolItemFactory.of(control);
    }

    public static ComboFactory combo(int style) {
      return ComboFactory.newCombo(style);
    }

    public static ToolBarFactory toolBar(int style) {
      return ToolBarFactory.newToolBar(style);
    }

    public static ToolItemFactory toolItem(int style) {
      return ToolItemFactory.newToolItem(style);
    }
  }

  public static final class C {
    private C() {}

    public static CComboFactory combo(int style) {
      return CComboFactory.newCombo(style);
    }

    public static CTabFolderFactory tabFolder(int style) {
      return CTabFolderFactory.newCTabFolder(style);
    }

    public static CTabItemFactory tabItem(int style) {
      return CTabItemFactory.newTabItem(style);
    }
  }
}
