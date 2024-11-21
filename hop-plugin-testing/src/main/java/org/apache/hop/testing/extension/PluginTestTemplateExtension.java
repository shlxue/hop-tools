package org.apache.hop.testing.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.stream.Stream;

class PluginTestTemplateExtension implements TestTemplateInvocationContextProvider {

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return false;
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    return Stream.empty();
  }
}
