package org.apache.hop.testing.extension;

import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public final class TestTemplates {
  private TestTemplates() {}

  public static TestTemplateInvocationContextProvider swtUiProvider() {
    return new SwtTestTemplateExtension();
  }

  public static TestTemplateInvocationContextProvider pluginUiProvider() {
    return new PluginUiTestTemplateExtension();
  }

  public static TestTemplateInvocationContextProvider pluginProvider() {
    return new PluginTestTemplateExtension();
  }

  public static TestTemplateInvocationContextProvider engineProvider() {
    return new EngineTestTemplateExtension();
  }
}
