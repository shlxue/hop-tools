package org.apache.hop.testing.extension;

import org.apache.hop.core.Result;
import org.apache.hop.pipeline.engine.IEngineComponent;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.slf4j.Logger;

import java.util.stream.Stream;

class PluginTestTemplateExtension extends BaseTestTemplateProvider {
  PluginTestTemplateExtension(Logger log) {
    super(log, new Class[] {IEngineComponent.class}, Result.class);
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    log.debug("Initializing test template for hop plugins");
    return Stream.empty();
  }
}
