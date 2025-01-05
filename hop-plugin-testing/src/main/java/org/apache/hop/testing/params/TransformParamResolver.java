package org.apache.hop.testing.params;

import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class TransformParamResolver<T extends ITransform> extends BasePluginParam<T, ITransformMeta> {

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return false;
  }

  @Override
  protected T creator(Constructor<T> constructor, ExtensionContext context)
      throws ReflectiveOperationException {
    return null;
  }
}
