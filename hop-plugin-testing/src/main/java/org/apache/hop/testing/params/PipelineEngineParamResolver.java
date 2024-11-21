package org.apache.hop.testing.params;

import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;

class PipelineEngineParamResolver
    extends GenericTypeParamResolver<IPipelineEngine<PipelineMeta>, INamedParameters> {

  PipelineEngineParamResolver() {
    super(true);
  }

  @Override
  protected boolean matchConstructor(Constructor<?> constructor) {
    return false;
  }

  @Override
  protected IPipelineEngine<PipelineMeta> creator(
      Constructor<IPipelineEngine<PipelineMeta>> constructor, ExtensionContext context)
      throws ReflectiveOperationException {
    return null;
  }
}
