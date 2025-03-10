package org.apache.hop.testing.params;

import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.resource.IResourceExport;

import java.lang.reflect.ParameterizedType;

abstract class BaseEngineParam<E extends INamedParameters, M extends IResourceExport>
    extends GenericTypeParamResolver<E, M> {

  protected final M meta;

  protected BaseEngineParam(M meta) {
    super(true);
    this.meta = meta;
  }

  @Override
  protected boolean matchParamType(Class<?> supportedClazz, Class<?> supportedInterface) {
    if (super.paramType instanceof ParameterizedType type) {
      return ((Class) type.getRawType()).isAssignableFrom(supportedClazz);
    }
    return false;
  }
}
