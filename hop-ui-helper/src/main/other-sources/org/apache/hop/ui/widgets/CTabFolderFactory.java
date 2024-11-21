package org.apache.hop.ui.widgets;

import org.eclipse.swt.custom.CTabFolder;

public final class CTabFolderFactory
    extends AbstractCompositeFactory<CTabFolderFactory, CTabFolder> {
  private CTabFolderFactory(int style) {
    super(CTabFolderFactory.class, parent -> new CTabFolder(parent, style));
  }

  public static CTabFolderFactory newCTabFolder(int style) {
    return new CTabFolderFactory(style);
  }
}
