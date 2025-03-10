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

  public static TestTemplateInvocationContextProvider pipelineEngineProvider() {
    return new PipelineEngineExtension();
  }

  public static TestTemplateInvocationContextProvider workflowEngineProvider() {
    return new WorkflowEngineExtension();
  }
}
