package org.apache.hop.testing.junit;

import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.config.plugin.ConfigFile;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.projects.config.ProjectsConfig;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
class HopJunitConfig extends ConfigFile {
  static final String DEFAULT_PROJECT_IT_FOLDER = "integration-tests";
  static final Pattern jarPrefix = Pattern.compile("^hop-(transform|action|databases)-\\w+-");

  private final IVariables variables;
  private final Path baseDir;
  private final Path testClassPath;
  private final Path[] classPath;
  private final Path itProjectDir;
  private final URI[] hopPluginJars;
  @Setter private String configFilename;

  HopJunitConfig(IVariables variables) {
    this.variables = variables;
    Set<Path> paths = ReflectionUtils.getAllClasspathRootDirectories();
    Path pathPrefix = Paths.get(System.getProperty("user.dir"));
    this.baseDir = searchBaseDir(paths, pathPrefix);
    this.itProjectDir = searchItRootProject(baseDir, DEFAULT_PROJECT_IT_FOLDER);
    this.classPath = getClassPath(paths, pathPrefix, false);
//    this.testClassPath = getClassPath(paths, pathPrefix, true)[0];
//    this.testClassPath = Paths.get(".");
    testClassPath = null;
    this.hopPluginJars = searchHopPluginJars();
    applySystemProperties();
    this.configFilename = Const.HOP_CONFIG_FOLDER + Const.FILE_SEPARATOR + Const.HOP_CONFIG;
    Path configFile = Paths.get(configFilename);
    if (!Files.exists(configFile)) {
      dumpConfigTemplate(configFile);
    }
    Properties props = System.getProperties();
    props.stringPropertyNames().forEach(k -> variables.setVariable(k, props.getProperty(k)));
  }

  private Path searchBaseDir(Set<Path> subPaths, Path currentPath) {
    Path latest = currentPath;
    for (Path path : subPaths) {
      while (path != null) {
        Path gitPath = path.resolve(".git");
        if (Files.isDirectory(gitPath)) {
          return path;
        }
        if (Files.exists(path.resolve("pom.xml")) && latest.startsWith(path)) {
          latest = path;
        }
        path = path.getParent();
      }
    }
    return latest;
  }

  private Path searchItRootProject(Path baseDir, String defaultFolder) {
    Path projectFile = Paths.get(defaultFolder, ProjectsConfig.DEFAULT_PROJECT_CONFIG_FILENAME);
    Predicate<Path> filter = path -> Files.exists(path.resolve(projectFile));
    if (filter.test(baseDir)) {
      return baseDir.resolve(defaultFolder);
    }
    try (Stream<Path> stream = Files.walk(baseDir, 2)) {
      return stream.filter(Files::isDirectory).filter(filter).findFirst().orElse(null);
    } catch (IOException e) {
      // ignore
    }
    return null;
  }

  private URI[] searchHopPluginJars() {
    String jvmClassPath = ManagementFactory.getRuntimeMXBean().getClassPath();
    if (StringUtils.isNotBlank(jvmClassPath)) {
      Path prefix = Paths.get(System.getProperty("user.dir"), "plugins").toAbsolutePath();
      return Arrays.stream(jvmClassPath.split(":"))
          .filter(s -> s.endsWith(".jar"))
          .map(Paths::get)
          .map(Path::toAbsolutePath)
          .filter(path -> !path.startsWith(prefix))
          .filter(this::isHopPluginJar)
          .map(Path::toUri)
          .toArray(URI[]::new);
    }
    return new URI[0];
  }

  private boolean isHopPluginJar(Path jarPath) {
    String name = jarPath.getFileName().toString();
    if (name.startsWith("hop-")) {
      return jarPrefix.matcher(name).find();
    }
    return false;
  }

  private void applySystemProperties() {
    System.setProperty(Const.HOP_DISABLE_CONSOLE_LOGGING, "Y");
    System.setProperty(Const.HOP_REDIRECT_STDERR, "N");
    System.setProperty(Const.HOP_REDIRECT_STDOUT, "N");
    System.setProperty("HOP_CONFIG_FOLDER", testClassPath + "/config");
    System.setProperty("HOP_AUDIT_FOLDER", testClassPath + "/audit");
    System.setProperty("HOP_USE_NATIVE_FILE_DIALOG", "true");
    System.setProperty(Props.STRING_SHOW_TABLE_VIEW_TOOLBAR, "Y");
  }

  private void dumpConfigTemplate(Path configFile) {
    String templateFile = "/config/" + configFile.getFileName().toString();
    try (InputStream is = getClass().getResourceAsStream(templateFile)) {
      if (is != null) {
        if (!Files.exists(configFile.getParent())) {
          Files.createDirectories(configFile.getParent());
        }
        Files.write(configFile, is.readAllBytes());
      }
    } catch (IOException e) {
      throw new IllegalStateException("Error reading " + templateFile, e);
    }
  }

  private Path[] getClassPath(Set<Path> classPaths, Path pathPrefix, boolean excludeTestScope) {
    Path pathSuffix = excludeTestScope ? Paths.get("classes") : Paths.get("test-classes");
    Set<Path> filter =
        classPaths.stream()
            .filter(path -> path.startsWith(pathPrefix))
            .filter(path -> path.endsWith(pathSuffix))
            .collect(Collectors.toSet());
    if (filter.isEmpty()) {
      getModuleJar(pathPrefix.toString()).ifPresent(filter::add);
    }
    return filter.toArray(new Path[0]);
  }

  private Optional<Path> getModuleJar(String basePath) {
    String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
    return Arrays.stream(paths)
        .filter(s -> s.endsWith(".jar"))
        .filter(s -> s.startsWith(basePath))
        .map(Paths::get)
        .findFirst();
  }
}
