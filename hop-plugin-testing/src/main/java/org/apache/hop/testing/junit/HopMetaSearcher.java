package org.apache.hop.testing.junit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.hop.core.Const;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.projects.config.ProjectsConfig;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.workflow.WorkflowMeta;
import org.junit.platform.commons.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class HopMetaSearcher {
  private static final Pattern PLUGIN_PATTERN = Pattern.compile("<(?:transform|action)>");
  private static final Pattern NAME_PATTERN = Pattern.compile("<name>(.*)</name>");
  private static final Pattern TYPE_PATTERN = Pattern.compile("<type>(\\w+)</type>");
  private final Logger log = LoggerFactory.getLogger(HopMetaSearcher.class);
  private final List<ProjectCache> projects = new ArrayList<>(32);
  private final Set<Path> subPaths = new HashSet<>();
  private Path rootPath;
  private final ProjectsConfig projectsConfig;
  private final ProjectConfig mockProjectConfig;

  public HopMetaSearcher() {
    projectsConfig = new ProjectsConfig();
    mockProjectConfig = new ProjectConfig();
    mockProjectConfig.setProjectName("default");
  }

  public ProjectConfig getParent() {
    return mockProjectConfig;
  }

  public void loading(IVariables variables, Path rootPath, Path subPath) {
    if (subPaths.contains(subPath)) {
      return;
    }
    subPaths.add(subPath);
    this.rootPath = Preconditions.notNull(rootPath, "Hop project path must not be null");
    mockProjectConfig.setProjectHome(rootPath.toString());
    try (Stream<Path> walk = Files.list(rootPath).filter(Files::isDirectory)) {
      walk.parallel()
          .filter(path -> Files.exists(path.resolve(Const.HOP_CONFIG)))
          .forEach(path -> searchMetaFiles(variables, path));
    } catch (IOException e) {
      throw new IllegalStateException("Search meta files from " + rootPath, e);
    }
  }

  private void searchMetaFiles(IVariables variables, Path basePath) {
    ProjectConfig config =
        new ProjectConfig(basePath.getFileName().toString(), basePath.toString(), Const.HOP_CONFIG);
    try (Stream<Path> stream = Files.walk(basePath).filter(this::isMetaFile)) {
      ProjectCache projectInfo = new ProjectCache(config);
      stream
          .parallel()
          .forEach(path -> buildCache(path).ifPresent(infos -> projectInfo.put(path, infos)));
      synchronized (projectsConfig) {
        projectsConfig.addProjectConfig(config);
        projects.add(projectInfo);
      }
    } catch (Exception ignore) {
      log.warn("Error loading project config", ignore);
    }
  }

  public Map<ProjectConfig, List<Path>> search(
      boolean inPipeline,
      String pluginId,
      Predicate<String> projectFilter,
      Predicate<String> envFilter,
      Predicate<Path> nameFilter) {
    Map<ProjectConfig, List<Path>> result = new LinkedHashMap<>();
    Predicate<String> pluginIdFilter = StringUtil.isEmpty(pluginId) ? id -> true : id -> id.equals(pluginId);
//    Predicate<String> loadedPluginFilter = loadedPluginIds::contains;
    for (ProjectCache cache : projects) {
      if (!projectFilter.test(cache.projectConfig.getProjectName())) {
        continue;
      }
      Map<Path, Set<PluginInfo>> map = inPipeline ? cache.pipelinePlugins : cache.workflowPlugins;
      List<Path> list = map.entrySet().stream()
//          .filter(entry -> nameFilter.test(entry.getKey()))
          .filter(entry -> entry.getValue().stream().map(PluginInfo::getId).anyMatch(pluginIdFilter))
          .map(Map.Entry::getKey)
          .sorted()
          .toList();
      if (!list.isEmpty()) {
        result.put(cache.projectConfig, list);
      }
    }
    return result;
  }

  public boolean supportAllPlugins(boolean isPipeline, Path path, Predicate<String> filter, Consumer<String> missingConsumer) {
    for (ProjectCache cache : projects) {
      Map<Path, Set<PluginInfo>> map = isPipeline ? cache.pipelinePlugins : cache.workflowPlugins;
      if (!map.containsKey(path)) {
        continue;
      }
      Set<PluginInfo> infos = map.get(path);
      if (!infos.stream().allMatch(info -> filter.test(info.id))) {
        infos.stream().map(PluginInfo::getId).filter(id -> !filter.test(id)).forEach(missingConsumer);
        return false;
      }
      break;
    }
    return true;
  }

  private boolean isMetaFile(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.endsWith(PipelineMeta.PIPELINE_EXTENSION)
        || fileName.endsWith(WorkflowMeta.WORKFLOW_EXTENSION);
  }

  private Optional<Set<PluginInfo>> buildCache(Path metaFile) {
    try {
      List<String> lines = Files.readAllLines(rootPath.resolve(metaFile), StandardCharsets.UTF_8);
      Map<String, Set<String>> map = new HashMap<>();
      int count = lines.size();
      int i = 0;
      while (i < count) {
        Matcher matcher = PLUGIN_PATTERN.matcher(lines.get(i++).trim());
        if (matcher.matches()) {
          Matcher nameMatcher = null;
          Matcher typeMatcher = null;
          for (int j = 0; j < 3; j++) {
            String line = lines.get(i++).trim();
            Optional<Matcher> optional = nameMatcher == null ? matcherLine(NAME_PATTERN, line) : Optional.empty();
            if (optional.isPresent()) {
              nameMatcher = optional.get();
              continue;
            }
            optional = typeMatcher == null ? matcherLine(TYPE_PATTERN, line) : Optional.empty();
            if (optional.isPresent()) {
              typeMatcher = optional.get();
            }
          }
          if (typeMatcher != null && nameMatcher != null) {
            map.computeIfAbsent(typeMatcher.group(1), k -> new HashSet<>()).add(nameMatcher.group(1));
          }
        }
      }
      if (!map.isEmpty()) {
        Set<PluginInfo> pluginInfos = new HashSet<>(map.size());
        map.forEach((k, v) -> pluginInfos.add(new PluginInfo(k, v.toArray(new String[0]))));
        return Optional.of(pluginInfos);
      }
    } catch (IOException e) {
      log.warn("Unable to read meta file {}", metaFile, e);
    }
    return Optional.empty();
  }

  private Optional<Matcher> matcherLine(Pattern pattern, String line) {
    Matcher matcher = pattern.matcher(line);
    return matcher.find() ? Optional.of(matcher) : Optional.empty();
  }

  private static class ProjectCache {
    private final ProjectConfig projectConfig;
    private final Path homePath;
    private final Map<Path, Set<PluginInfo>> pipelinePlugins = new ConcurrentHashMap<>(256);
    private final Map<Path, Set<PluginInfo>> workflowPlugins = new ConcurrentHashMap<>(256);

    public ProjectCache(ProjectConfig projectConfig) {
      this.projectConfig = projectConfig;
      homePath = Paths.get(projectConfig.getProjectHome());
    }

    void put(Path path, Set<PluginInfo> infos) {
      path = homePath.relativize(path);
      if (path.toString().endsWith(PipelineMeta.PIPELINE_EXTENSION)) {
        pipelinePlugins.put(path, infos);
      } else {
        workflowPlugins.put(path, infos);
      }
    }
  }

  @Getter
  @RequiredArgsConstructor
  static class PluginInfo {
    private final String id;
    private final String[] names;
  }
}
