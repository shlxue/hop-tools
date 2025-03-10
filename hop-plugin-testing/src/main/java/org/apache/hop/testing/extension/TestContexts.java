package org.apache.hop.testing.extension;

import org.apache.hop.base.AbstractMeta;
import org.apache.hop.core.file.IHasFilename;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.testing.condition.EnableOnX11Condition;
import org.apache.hop.testing.condition.EnableSwtEnvironment;
import org.apache.hop.workflow.WorkflowMeta;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.function.Supplier;

public class TestContexts {

  private static final ExecutionCondition[] UI_CONDITION_EXTENSIONS =
      new ExecutionCondition[]{new EnableOnX11Condition(), new EnableSwtEnvironment()};

  private TestContexts() {
  }

  static TestTemplateInvocationContext ofSwt(
      String displayName, ParameterResolver root, InvocationInterceptor... specs) {
    return new SwtTestContext(displayName, root, specs);
  }

  static TestTemplateInvocationContext ofUi(
      String displayName, ParameterResolver root, InvocationInterceptor... specs) {
    return new UiTestContext(displayName, root, specs);
  }

  static TestTemplateInvocationContext ofWorkflowEngine(
      ProjectConfig config, WorkflowMeta workflowMeta, ParameterResolver resolver, InvocationInterceptor... specs) {
    return new EngineTestContext<>("Workflow", config, workflowMeta, () -> metaName(workflowMeta), resolver, specs);
  }

  static TestTemplateInvocationContext ofPipelineEngine(
      ProjectConfig config, PipelineMeta pipelineMeta, ParameterResolver resolver, InvocationInterceptor... specs) {
    return new EngineTestContext<>("Pipeline", config, pipelineMeta, () -> metaName(pipelineMeta), resolver, specs);
  }

  private static <Meta extends AbstractMeta> String metaName(Meta meta) {
    String filename = meta.getFilename();
    if (!StringUtil.isEmpty(filename)) {
      filename = filename.replaceFirst("\\.h(wf|pl)$", "");
    }
    return StringUtil.isEmpty(filename) ? meta.getName() : filename;
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

  private static class EngineTestContext<T extends IHasFilename> extends TemplateContext {
    private final ProjectConfig config;
    private final T meta;

    EngineTestContext(String category, ProjectConfig config, T pipelineMeta, Supplier<String> name, ParameterResolver resolver, InvocationInterceptor... specs) {
      super(category, name.get(), new ExecutionCondition[0], resolver, specs);
      this.config = config;
      this.meta = pipelineMeta;
//      super.extensions.add(ParameterResolvers.);
    }

    @Override
    public String getDisplayName(int invocationIndex) {
      return String.format("[%s] %x. %s in <%s>", category, invocationIndex, displayName, config.getProjectName());
    }
  }
}
