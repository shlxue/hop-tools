package org.apache.hop.testing.params;

import org.apache.hop.core.IExtensionData;

abstract class BasePluginParam<T extends IExtensionData, I> extends GenericTypeParamResolver<T, I> {
  protected BasePluginParam() {
    super(true);
  }
}
