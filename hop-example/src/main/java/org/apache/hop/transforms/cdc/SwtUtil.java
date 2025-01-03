package org.apache.hop.transforms.cdc;

import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.TransformPluginType;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.transforms.cdc.jdbc.Index;
import org.apache.hop.transforms.cdc.jdbc.Tab;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.StyledTextComp;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.layout.LData;
import org.apache.hop.ui.layout.Layouts;
import org.apache.hop.ui.widgets.ButtonFactory;
import org.apache.hop.ui.widgets.LabelFactory;
import org.apache.hop.ui.widgets.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.*;

final class SwtUtil {
  private static final int TABLE_MIN_LINE = 3;
  private static final int TABLE_MAX_LINE = 5;
  //  private static final LineStyleListener LINE_STYLE_LISTENER = new SQLValuesHighlight();

  //// GridLayout
  static void hintHeight(Table table) {
    hintHeight(table, TABLE_MIN_LINE);
  }

  static void hintHeight(Table table, int minLine) {
    hintHeight(table, minLine, TABLE_MAX_LINE);
  }

  static void hintHeight(Table table, int minLine, int maxLine) {
    Composite parent = table.getParent();
    if (parent instanceof TableView && parent.getLayoutData() instanceof GridData) {
      GridData gridData = (GridData) parent.getLayoutData();
      int count = Math.max(minLine, table.getItemCount());
      if (maxLine > 0) {
        count = Math.min(count, maxLine);
      }
      gridData.heightHint = (table.getItemHeight() * count + table.getHeaderHeight());
      parent.getParent().layout();
    }
  }

  static void setShellImage(Shell shell, Button wHelp, TransformMeta stepMeta) {
    if (stepMeta != null) {
      IPlugin plugin =
          PluginRegistry.getInstance()
              .getPlugin(TransformPluginType.class, stepMeta.getTransform());
      String id = plugin.getIds()[0];
      if (id != null) {
        shell.setImage(
            GuiResource.getInstance()
                .getImagesTransforms()
                .get(id)
                .getAsBitmapForSize(shell.getDisplay(), ConstUi.ICON_SIZE, ConstUi.ICON_SIZE));
      }
      wHelp.setImage(GuiResource.getInstance().getImageHelpWeb());
      wHelp.setText("");
      // TODO Help button
      //      wHelp.addSelectionListener(
      //          SelectionListener.widgetSelectedAdapter(
      //              event -> HelpUtils.openHelpDialog(shell, plugin)));
    }
  }

  //// TextVar for database
  //  static Text newText(Composite parent, int style, IVariables space, Consumer<ButtonFactory>
  // apply, Consumer<SelectionEvent> onSelect) {
  //    Composite wComposite =
  //        Widgets.composite(SWT.NONE)
  //            .layout(Layouts.grid(2).margin(0).clean(true, true).get())
  //            .layoutData(Ld.Editor.field())
  //            .create(parent);
  //    T wControl = creator.apply(wComposite);
  //    wControl.setLayoutData(Ld.grid(GridData.FILL_HORIZONTAL).get());
  //    ButtonFactory buttonFactory =
  //
  // Widgets.button(SWT.PUSH).layoutData(Ld.grid(GridData.HORIZONTAL_ALIGN_END).hint(120).get());
  //
  //    apply.accept(buttonFactory);
  //    buttonFactory.create(wComposite);
  //    return wControl;
  //  }

  static <T extends Control> T newFieldWithButton(
      Composite parent, Function<Composite, T> creator, UnaryOperator<ButtonFactory> apply) {
    Composite wComposite =
        Widgets.composite(SWT.NONE)
            .layout(Layouts.grid(2).margin(0).clean(true, true).get())
            //            .layoutData(LData.field())
            .create(parent);
    T wControl = creator.apply(wComposite);
    wControl.setLayoutData(LData.grid(GridData.FILL_HORIZONTAL).get());
    ButtonFactory buttonFactory =
        Widgets.button(SWT.PUSH)
            .layoutData(LData.grid(GridData.HORIZONTAL_ALIGN_END).hint(120).get());

    apply.apply(buttonFactory).create(wComposite);
    return wControl;
  }

  //// StyledText for sql
  static StyledTextComp newStyledText(Supplier<StyledTextComp> creator, GridData layoutData) {
    StyledTextComp styledTextComp = creator.get();
    styledTextComp.setLayoutData(layoutData);
    //    styledTextComp.addLineStyleListener(LINE_STYLE_LISTENER);
    return null; // styledTextComp.getStyledText();
  }

  static Index<IValueMeta> toIndex(Tab<IValueMeta> tab, IRowMeta rowMeta, Object[] row) {
    try {
      Index<IValueMeta> index =
          Index.build(!rowMeta.getBoolean(row, 0), rowMeta.getString(row, 1), IValueMeta::getName);
      index.setType(Index.Type.valueOf(rowMeta.getInteger(row, 2).intValue()));
      index.setCardinality(rowMeta.getInteger(row, 3));
      index.setFields(tab.getFields(rowMeta.getString(row, 4)));
      return index;
    } catch (HopValueException e) {
      throw new IllegalStateException(e);
    }
  }

  static void fillField(TableItem item, IValueMeta valueMeta) {
    item.setData("model.field", valueMeta);
    item.setText(1, valueMeta.getName());
    item.setText(2, IValueMeta.getTypeDescription(valueMeta.getType()));
    if (valueMeta.isOriginalNullable() == DatabaseMetaData.columnNoNulls) {
      item.setText(3, "Y");
    }
    if (valueMeta.getLength() != -1) {
      item.setText(4, Integer.toString(valueMeta.getLength()));
      if (valueMeta.getPrecision() != -1) {
        item.setText(5, Integer.toString(valueMeta.getPrecision()));
      }
    }
    if (!Utils.isEmpty(valueMeta.getComments())) {
      item.setText(6, valueMeta.getComments());
    }
  }

  static void fillIndex(TableItem item, Index<?> index) {
    item.setData("model.index", index);
    item.setText(1, Const.nullToEmpty(index.getName()));
    if (index.isUnique()) {
      item.setText(2, "Y");
    }
    Object val = item.getParent().getData("model.count");
    if (val instanceof Long) {
      int coverage = index.getCoverage((Long) val);
      if (coverage != -1) {
        item.setText(3, Integer.toString(coverage));
      }
    }
    item.setText(4, Const.nullToEmpty(index.getFieldNames()));
    if (index.getType() != Index.Type.NONE) {
      item.setText(5, index.getType().name());
    }
  }

  static <T> void fillTableView(
      Table viewer, Collection<T> list, BiConsumer<TableItem, T> consumer) {
    viewer.setItemCount(list.size());
    Iterator<T> it = list.iterator();
    int i = 0;
    while (it.hasNext()) {
      TableItem item = viewer.getItem(i);
      i++;
      item.setText(0, Integer.toString(i));
      consumer.accept(item, it.next());
    }
    if (viewer.getParent() instanceof TableView) {
      ((TableView) viewer.getParent()).optWidth(true);
    }
  }

  //// Table schema for TableView
  static ColumnInfo[] indexColInfo() {
    ColumnInfo[] cols =
        readOnly(
            new ColumnInfo(i18n("System.Column.Name"), ColumnInfo.COLUMN_TYPE_TEXT),
            new ColumnInfo(i18n("CTL.Column.Unique"), ColumnInfo.COLUMN_TYPE_TEXT),
            new ColumnInfo(i18n("CTL.Column.Coverage"), ColumnInfo.COLUMN_TYPE_TEXT, true),
            new ColumnInfo(i18n("CTL.Column.FieldNames"), ColumnInfo.COLUMN_TYPE_TEXT),
            new ColumnInfo(i18n("CTL.Column.Type"), ColumnInfo.COLUMN_TYPE_TEXT));
    cols[2].setToolTip(i18n("CTL.ToolTip.ConvertRate"));
    return cols;
  }

  static ColumnInfo[] fieldColInfo() {
    return readOnly(
        new ColumnInfo(i18n("System.Column.Name"), ColumnInfo.COLUMN_TYPE_TEXT),
        new ColumnInfo(i18n("System.Column.Type"), ColumnInfo.COLUMN_TYPE_TEXT),
        new ColumnInfo(i18n("CTL.Column.NoNull"), ColumnInfo.COLUMN_TYPE_TEXT),
        new ColumnInfo(i18n("System.Column.Length"), ColumnInfo.COLUMN_TYPE_TEXT, true),
        new ColumnInfo(i18n("System.Column.Precision"), ColumnInfo.COLUMN_TYPE_TEXT, true),
        new ColumnInfo(i18n("CTL.Column.Remarks"), ColumnInfo.COLUMN_TYPE_TEXT));
  }

  static ColumnInfo[] tableColInfo() {
    ColumnInfo[] cols =
        readOnly(
            new ColumnInfo(i18n("CTL.Column.Name"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[0]),
            new ColumnInfo(i18n("CTL.Column.KeyFields"), ColumnInfo.COLUMN_TYPE_TEXT),
            new ColumnInfo(i18n("CTL.Column.Unique"), ColumnInfo.COLUMN_TYPE_TEXT),
            new ColumnInfo(i18n("CTL.Column.Cardinality"), ColumnInfo.COLUMN_TYPE_TEXT, true),
            new ColumnInfo(i18n("CTL.Column.IgnoreDel"), ColumnInfo.COLUMN_TYPE_TEXT),
            new ColumnInfo(i18n("CTL.Column.IndexName"), ColumnInfo.COLUMN_TYPE_TEXT),
            new ColumnInfo(i18n("CTL.Column.Type"), ColumnInfo.COLUMN_TYPE_TEXT));
    cols[0].setReadOnly(false);
    cols[5].setToolTip(i18n("CTL.ToolTip.Cardinality.Table"));
    cols[6].setToolTip(i18n("CTL.ToolTip.KeyFields.Sorted"));
    return cols;
  }

  //// SWT
  static Event newEvent(Widget source) {
    return newEvent(source, null, null);
  }

  static Event newEvent(Widget source, Widget item) {
    return newEvent(source, item, null);
  }

  static Event newEvent(Widget source, Consumer<Event> consumer) {
    return newEvent(source, null, consumer);
  }

  static void tableEvent(Event event) {
    if (event.widget instanceof Table) {
      int index = ((Table) event.widget).getSelectionIndex();
      if (index >= 0) {
        event.item = ((Table) event.widget).getItem(0);
      }
    }
  }

  static Event newEvent(Widget source, Widget item, Consumer<Event> consumer) {
    Event e = new Event();
    e.widget = source;
    e.display = source.getDisplay();
    e.item = item;
    if (consumer != null) {
      consumer.accept(e);
    }
    return e;
  }

  //// Field in form dialog
  static void newLabel(Composite parent, String text) {
    newLabel(parent, text, false);
  }

  static void newLabel(Composite parent, String text, boolean rightAtTop) {
    LabelFactory factory = Widgets.label(SWT.RIGHT).text(text);
    if (parent.getLayout() instanceof GridLayout) {
      //      factory.layoutData(rightAtTop ? LData.Editor.labelAtTop() : LData.Editor.label());
    }
    factory.create(parent);
  }

  static boolean applyLook(Composite parent, PropsUi props) {
    boolean hasCustom = false;
    for (Control control : parent.getChildren()) {
      if (control instanceof Composite) {
        hasCustom = applyLook((Composite) control, props);
      }
    }
    if (!hasCustom) {
      props.setLook(parent, Props.WIDGET_STYLE_DEFAULT);
    }
    return true;
  }

  //// private
  private static ColumnInfo[] readOnly(ColumnInfo... cols) {
    for (ColumnInfo col : cols) {
      col.setReadOnly(true);
    }
    return cols;
  }

  private static String i18n(String key) {
    return Util.i18n(SwtUtil.class, key);
  }
}
