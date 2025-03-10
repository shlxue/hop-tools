package org.apache.hop.testing.extension;

import org.apache.hop.core.Result;
import org.apache.hop.core.annotations.Action;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.testing.engine.WorkflowEngineProvider;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.testing.params.ParameterResolvers;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

class WorkflowEngineExtension extends BaseTestTemplateProvider<IAction> {

  WorkflowEngineExtension() {
    super(IAction.class, new Class[]{IWorkflowEngine.class}, Result.class);
  }

  @Override
  protected String getPluginId(Class<?> pluginClass) {
    return pluginClass.getAnnotation(Action.class).id();
  }

  @Override
  protected Stream<TestTemplateInvocationContext> buildContexts(
      ExtensionContext context, Class<?>... paramTypes) {
//    ParameterResolver paramResolver =
//        Arrays.stream(paramTypes)
//            .map(this::pluginUiParamResolver)
//            .filter(Objects::nonNull)
//            .findFirst()
//            .orElse(null);
//    if (paramResolver == null) {
//      paramResolver = null;
//    }
    Class<?> transformClass = StatusUtil.get(context, StoreKey.HOP_JUNIT_PLUGIN_METAS, Class.class);
    String  id = transformClass.getAnnotation(Action.class).id();
    return
        hopJunit.findWorkflows(id, this::filterProject, this::filterEnv, this::filterNames)
            .entrySet().stream()
            .flatMap(entry -> buildItContexts(entry.getKey(), entry.getValue()));
  }

  private Stream<TestTemplateInvocationContext> buildItContexts(ProjectConfig config, List<Path> files) {
    Path basePath = Paths.get(config.getProjectHome());
    return files.stream().map(path -> buildContext(config, basePath, path));
  }

  private TestTemplateInvocationContext buildContext(ProjectConfig config, Path basePath, Path path) {
    try (InputStream xmlStream = Files.newInputStream(basePath.resolve(path))) {
      WorkflowMeta meta = new WorkflowMeta(xmlStream, hopJunit.getMetadataProvider(), hopJunit.getVariables());
      meta.setFilename(path.toString());
      return TestContexts.ofWorkflowEngine(config, meta, ParameterResolvers.workflowEngine(meta), new WorkflowEngineProvider());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (HopException e) {
      throw new RuntimeException(e);
    }
  }

//  private ParameterResolver pluginUiParamResolver(Class<?> paramType) {
//    if (IAction.class.isAssignableFrom(paramType)
//        || IWorkflowEngine.class.isAssignableFrom(paramType)) {
//      return ParameterResolvers.workflowEngine();
//    }
//    return null;
//  }
}
