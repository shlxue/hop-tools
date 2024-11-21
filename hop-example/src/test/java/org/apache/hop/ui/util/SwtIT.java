package org.apache.hop.ui.util;

import org.apache.hop.core.util.StringUtil;
import org.apache.hop.testing.HopExtension;
import org.apache.hop.ui.widgets.Adapter;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@ExtendWith(HopExtension.class)
class SwtIT {
  static String[] items;

  static {
    try {
      items = Files.readAllLines(Paths.get("/Users/shl/a.list")).toArray(String[]::new);
    } catch (IOException ignore) {
    }
  }

  @TestTemplate
  void name(CCombo wCombo) {
    wCombo.setItems(items);
    //    wCombo.setData("items", wCombo.getItems());
    //    wCombo.notifyListeners(13, new Event());
    wCombo.setListVisible(true);
    wCombo.addModifyListener(Adapter.modify(this::onModify));
  }

  private void onModify(ModifyEvent event) {
    CCombo wCombo = (CCombo) event.getSource();
    String input = wCombo.getText();
    if (StringUtil.isEmpty(input)) {
      wCombo.setItems(items);
      return;
    }

    List<String> list = new LinkedList<>(Arrays.asList(items));
    List<String> cache = new ArrayList<>(items.length);
    wCombo.setRedraw(false);
    try {
      list.removeIf(
          s -> {
            if (s.startsWith(input)) {
              cache.add(s);
              return true;
            }
            return false;
          });
      list.removeIf(
          s -> {
            if (s.contains(input)) {
              cache.add(s);
              return true;
            }
            return false;
          });
      if (cache.isEmpty()) {
        wCombo.setItems(items);
      } else {
        wCombo.setItems(cache.toArray(String[]::new));
      }
    } finally {
      wCombo.setRedraw(true);
    }
  }

  @TestTemplate
  void showDirectoryDialog(DirectoryDialog dialog) {
    dialog.setText("Select directory");
    //    dialog.setMessage("a message");
    dialog.setFilterPath("/Users/shl");
    Assertions.assertNotNull(dialog);
  }

  @TestTemplate
  void showFileDialog(FileDialog dialog) {
    dialog.setText("Select file");
    //    dialog.setFileName("a.xml");
    //    dialog.setFilterExtensions(new String[]{"*.xml1"});
    dialog.setFilterExtensions(new String[] {"*.xml", "*.txt", "*.*"});
    //    dialog.setFilterNames(new String[] {"*.xml", "*.txt", "*.*"});
    dialog.setFilterIndex(2);
    dialog.setOverwrite(false);
    //    dialog.setMessage("a message");
    dialog.setFilterPath("/Users/shl");
  }
}
