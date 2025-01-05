package org.apache.hop.ui.util;

import lombok.Builder;
import org.apache.hop.ui.core.widget.TextVar;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import java.util.Optional;

@Builder(builderClassName = "Builder")
public final class VfsConfig {
  private final String title;
  private final String message;
  private final String basePath;
  private final String[] fileExtensions;
  private final String[] fileNames;
  private final Control editor;

  public static Builder of(String title) {
    return new Builder().title(title);
  }

  public String title() {
    return title;
  }

  public String message() {
    return message;
  }

  public String basePath() {
    return basePath;
  }

  public String[] fileExtensions() {
    return fileExtensions;
  }

  public String[] fileNames() {
    return fileNames;
  }

  public Control editor() {
    return editor;
  }

  public <T extends Control> Control editor(Class<T> type) {
    return editor != null ? type.cast(editor) : null;
  }

  public Optional<Combo> tryGetCombo() {
    if (editor instanceof Combo combo) {
      return Optional.of(combo);
    }
    return Optional.empty();
  }

  public Optional<Text> tryGetText() {
    if (editor instanceof Text text) {
      return Optional.ofNullable(text);
    }
    return Optional.empty();
  }

  public Optional<TextVar> tryGetTextVar() {
    if (editor instanceof Text && editor.getParent() instanceof TextVar) {
      return Optional.of((TextVar) editor.getParent());
    }
    return Optional.empty();
  }
}
