package org.apache.hop.testing.params;

import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.resource.IResourceExport;

abstract class BaseEngineParam<E extends INamedParameters, M extends IResourceExport>
    extends GenericTypeParamResolver<E, M> {

  protected BaseEngineParam() {
    super(true);
  }
}
