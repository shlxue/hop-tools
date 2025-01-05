package org.apache.hop.testing.extension;

import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestTemplates {
  private static final Logger log = LoggerFactory.getLogger(TestTemplates.class);

  private TestTemplates() {}

  public static TestTemplateInvocationContextProvider swtUiProvider() {
    return new SwtTestTemplateExtension(log);
  }

  public static TestTemplateInvocationContextProvider pluginUiProvider() {
    return new PluginUiTestTemplateExtension(log);
  }

  public static TestTemplateInvocationContextProvider pluginProvider() {
    return new PluginTestTemplateExtension(log);
  }

  public static TestTemplateInvocationContextProvider engineProvider() {
    return new EngineTestTemplateExtension(log);
  }
}
