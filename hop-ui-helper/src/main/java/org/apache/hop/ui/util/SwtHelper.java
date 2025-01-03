package org.apache.hop.ui.util;

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TextVar;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SwtHelper {
  private SwtHelper() {}

  /// editor
  public static void bind(Control target, Label wLabel) {
    Data.add(target, Data.UI_LABEL, wLabel);
  }

  public static void bind(Control target, Label wLabel, Button wButton) {
    bind(target, wLabel);
    Data.add(wButton, Data.UI_LABEL, wLabel);
    Data.add(wButton, Data.UI_EDITOR, target);
  }

  /// jdbc
  public static <E extends Control> void bind(Control wConnection, E wSchema) {
    bind(wConnection, wSchema, null);
  }

  public static <E extends Control> Control[] bind(Control wConnection, E wSchema, E wTable) {
    Data.add(wSchema, Data.UI_JDBC_CONNECTION, wConnection);
    if (wTable != null) {
      Data.add(wTable, Data.UI_JDBC_CONNECTION, wConnection);
      Data.add(wTable, Data.UI_JDBC_SCHEMA, wSchema);
      Data.tryGet(wTable, Control.class, Data.UI_ACTION)
          .ifPresent(c -> Data.add(c, Data.UI_JDBC_SCHEMA, wSchema));
    }
    //    Data.add(wConnection, Data.UI_JDBC_SCHEMA, wSchema);
    //    Data.add(wSchema, Data.UI_JDBC_TABLE, wTable);
    List<Control> list = new ArrayList<>();
    for (Control item : new Control[] {wConnection, wSchema, wTable}) {
      if (item != null) {
        Data.tryGet(item, Control.class, Data.UI_ACTION).ifPresent(list::add);
      }
    }
    list.forEach(c -> Data.add(c, Data.UI_JDBC_CONNECTION, wConnection));
    return list.toArray(new Control[0]);
  }

  ///  mixed
  ///
  public static <T extends Control, W extends Text> void bind(Type type, Button wButton, W target) {
    bind(type, wButton, target, target::getText);
  }

  public static <W extends Text> void bind(
      Type type, Button wButton, W target, Supplier<String> getter) {
    bind(type, wButton, target, getter, target::setText);
  }

  public static <T extends Control, W extends Control> void bind(
      Type type, Button wButton, W target, IVariables variables) {
    Supplier<String> getter;
    Consumer<String> setter;
    if (target instanceof Text wText && target.getParent() instanceof TextVar) {
      getter = wText::getText;
      setter = wText::setText;
    } else if (target instanceof Combo wCombo && target.getParent() instanceof ComboVar) {
      getter = wCombo::getText;
      setter = wCombo::setText;
    } else if (target instanceof TextVar wTextVar) {
      getter = wTextVar::getText;
      setter = wTextVar::setText;
    } else if (target instanceof ComboVar wComboVar) {
      getter = wComboVar::getText;
      setter = wComboVar::setText;
    } else {
      throw new IllegalArgumentException("Unsupported target type: " + target.getClass().getName());
    }
    bind(type, wButton, target, () -> variables.resolve(getter.get()), setter);
  }

  public static <T extends Control, W extends Widget> void bind(
      Type type, Button wButton, W target, Supplier<String> getter, Consumer<String> setter) {
    Data.add(wButton, Data.UI_EDITOR, target);
    Data.add(wButton, Data.UI_EDITOR_GETTER, getter);
    Data.add(wButton, Data.UI_EDITOR_SETTER, setter);
    Data.add(target, Data.UI_ACTION, wButton);
  }
}
