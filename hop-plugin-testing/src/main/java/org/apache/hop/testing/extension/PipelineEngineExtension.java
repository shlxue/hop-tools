package org.apache.hop.testing.extension;

import org.apache.hop.core.Result;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IEngineComponent;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.testing.engine.LocalPipelineEngineProvider;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.testing.params.ParameterResolvers;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

class PipelineEngineExtension extends BaseTestTemplateProvider<ITransform> {

  PipelineEngineExtension() {
    super(
        ITransform.class,
        new Class[]{IPipelineEngine.class, IEngineComponent.class, ITransformMeta.class},
        Result.class);
  }

  @Override
  protected Stream<TestTemplateInvocationContext> buildContexts(
      ExtensionContext context, Class<?>... paramTypes) {
    Class<?> transformClass = StatusUtil.get(context, StoreKey.HOP_JUNIT_PLUGIN_METAS, Class.class);
    if (transformClass == null) {
      transformClass = StatusUtil.get(context, StoreKey.HOP_JUNIT_PLUGINS, Class.class);
      if (transformClass != null) {
        transformClass = (Class<?>) ((ParameterizedType) transformClass.getGenericSuperclass()).getActualTypeArguments()[0];
      }
    }
    String id = transformClass != null ? transformClass.getAnnotation(Transform.class).id() : null;
//    ParameterResolver paramResolver =
//        Arrays.stream(paramTypes)
//            .map(this::pluginUiParamResolver)
//            .filter(Objects::nonNull)
//            .findFirst()
//            .orElse(null);
//    if (paramResolver == null) {
//      paramResolver = null;
//    }
    List<Path> skipping = new ArrayList<>();
    SortedSet<String> missing = new TreeSet<>();
    Map<ProjectConfig, List<Path>> map = hopJunit.findPipelines(id, this::filterProject, this::filterEnv, this::filterNames);
    List<TestTemplateInvocationContext> contexts = new ArrayList<>(map
        .entrySet().stream()
        .flatMap(entry -> buildItContexts(entry.getKey(), entry.getValue(), skipping, missing))
        .toList());
    if (!skipping.isEmpty()) {
      logger.warn("Skipping {} test files, missing plugins: {}", skipping.size(), String.join(",", missing));
    }
    if (transformClass != null) {
      contexts.add(0, buildMockContext(hopJunit.parentProjectConfig(), (Class<ITransformMeta>) transformClass));
    }
    return contexts.stream();
  }

  private TestTemplateInvocationContext buildMockContext(ProjectConfig config, Class<ITransformMeta> type) {
    PipelineMeta pipelineMeta = hopJunit.newPipelineMeta(type);
    return TestContexts.ofPipelineEngine(config, pipelineMeta, ParameterResolvers.pipelineEngine(pipelineMeta));
  }

  private Stream<TestTemplateInvocationContext> buildItContexts(ProjectConfig config, List<Path> files, List<Path> skipping, Set<String> missing) {
    Path basePath = Paths.get(config.getProjectHome());
    return files.stream()
        .filter(path -> {
          if (hopJunit.supported(path, missing)) {
            return true;
          }
          skipping.add(path);
          return false;
        })
        .map(path -> buildContext(config, basePath, path));
  }

  private TestTemplateInvocationContext buildContext(ProjectConfig config, Path basePath, Path path) {
    try (InputStream xmlStream = Files.newInputStream(basePath.resolve(path))) {
      PipelineMeta pipelineMeta = new PipelineMeta(xmlStream, hopJunit.getMetadataProvider(), hopJunit.getVariables());
      pipelineMeta.setFilename(path.toString());
      return TestContexts.ofPipelineEngine(config, pipelineMeta, ParameterResolvers.pipelineEngine(pipelineMeta), new LocalPipelineEngineProvider());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (HopException e) {
      throw new RuntimeException(e);
    }
  }

//  private ParameterResolver pluginUiParamResolver(Class<?> paramType) {
//    if (ITransform.class.isAssignableFrom(paramType)
//        || IPipelineEngine.class.isAssignableFrom(paramType)) {
//      return ParameterResolvers.pipelineEngine();
//    }
//    return null;
//  }

  @Override
  protected String getPluginId(Class<?> pluginClass) {
    if (pluginClass.getGenericSuperclass() instanceof ParameterizedType type
        && type.getActualTypeArguments().length == 2) {
      Type metaArg = type.getActualTypeArguments()[0];
      if (metaArg instanceof Class<?> metaType) {
        return metaType.getAnnotation(Transform.class).id();
      }
    }
    return null;
  }
}
