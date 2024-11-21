package org.apache.hop.ui.widgets;

public final class Widgets {
  private Widgets() {}

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
