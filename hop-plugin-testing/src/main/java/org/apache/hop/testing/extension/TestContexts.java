package org.apache.hop.testing.extension;

import org.apache.hop.testing.condition.EnableOnX11Condition;
import org.apache.hop.testing.condition.EnableSwtEnvironment;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

public class TestContexts {

  private static final ExecutionCondition[] UI_CONDITION_EXTENSIONS =
      new ExecutionCondition[] {new EnableOnX11Condition(), new EnableSwtEnvironment()};

  private TestContexts() {}

  static TestTemplateInvocationContext ofSwt(
      String displayName, ParameterResolver root, InvocationInterceptor... specs) {
    return new SwtTestContext(displayName, root, specs);
  }

  static TestTemplateInvocationContext ofUi(
      String displayName, ParameterResolver root, InvocationInterceptor... specs) {
    return new UiTestContext(displayName, root, specs);
  }

  static TestTemplateInvocationContext ofPlugin(
      String displayName, ParameterResolver root, InvocationInterceptor... specs) {
    return new PluginTestContext(displayName, root, specs);
  }

  static TestTemplateInvocationContext ofEngine(
      String displayName, ParameterResolver resolver, InvocationInterceptor... specs) {
    return new EngineTestContext(displayName, resolver, specs);
  }

  private static class SwtTestContext extends TemplateContext {
    SwtTestContext(String name, ParameterResolver resolver, InvocationInterceptor... specs) {
      super("SWT", name, UI_CONDITION_EXTENSIONS, resolver, specs);
    }
  }

  private static class UiTestContext extends TemplateContext {
    UiTestContext(String name, ParameterResolver resolver, InvocationInterceptor... specs) {
      super("UI", name, UI_CONDITION_EXTENSIONS, resolver, specs);
    }
  }

  private static class PluginTestContext extends TemplateContext {
    PluginTestContext(String name, ParameterResolver resolver, InvocationInterceptor... specs) {
      super("Plugin", name, new ExecutionCondition[0], resolver, specs);
    }
  }

  private static class EngineTestContext extends TemplateContext {
    EngineTestContext(String name, ParameterResolver resolver, InvocationInterceptor... specs) {
      super("Engine", name, new ExecutionCondition[0], resolver, specs);
    }
  }
}
