# HOP UI Best Practices

* Compatible SWT & RWT

ui extension modules

* hop-ui-helper

ui tools

* XWT: SWT & RWT
    * Widgets
    * Layouts & LData
    * SwtDialog
    * SwtHelper & Adapter
* HOP UI
    * Listeners
    * HopHelper
    * JdbcHelper
    * AsyncFetch

## Dialog code structure

```java
class TableInputDialog extends BaseTransformDialog {
    private static final Class<?> PKG = TableInputMeta.class;

    private final TextVar wSchema;
    private final TextVar wTable;

    public TableInputDialog(...) {
        super(..);

    }

    public String open() {
    }

    private

    private <T> void onChanged(T event) {
        input.hasChanged();
    }
}
```

## Dialog bast practices

1. fields
    * final control fields
    * local variable all labels
2. build ui
    * static ui: build ui in constructor method
    * dynamic ui: setup some listeners in open method
3. layout: based FormLayout
    * reactive editor
        * onTop
        * toRight & byRight
        * toLeft
        * fill
        * form
    * always bind to editor for label
        * on & onTop: bind label to editor
    * margin vs
4. Shell style
    * MIN & MAX
    * minimi
5. tab order
    * remove tab for all labels
6. GUI & Web Form
    * disable fill
        * all labels
        * check button
    * Check button without label
    * RWT & SWT
        * CCombo vs Combo
        * CTabFolder vs TabFolder
        * ~~Group~~ vs Label(SWT.SPECTOR)
    * Mixed Composite vs Ui helper
        * TextVar & ComboVar
        * StyleTextComp & ...
    * extension toolbar on TableView
    * extension buttons
        * Text & Browse button vs CCombo: like table editor
7. static listener: e -> input.hasChange();
    * addXxxListener(Consumer<T>) vs addListener(SWT.Selection, event)
8. dynamic listener:
9. async loading
    * prev transform fields
    * table fields
10. deprecated ui
    * getField button_
