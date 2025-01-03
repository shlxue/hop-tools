package org.apache.hop.maven.extensions;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MojoExecutionEvent;
import org.apache.maven.execution.MojoExecutionListener;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.logging.MessageBuilder;
import org.apache.maven.shared.utils.logging.MessageUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Component(role = MojoExecutionListener.class, hint = "first-thread-on-mac")
public class FirstThreadOnMacExtension implements MojoExecutionListener {
  private static final String START_ON_FIRST_THREAD_ARG = "-XstartOnFirstThread ";
  private static final String HOP_GROUP_ID = "org.apache.hop";
  private static final String HOP_TESTING_ARTIFACT_ID = "hop-plugin-testing";
  private static final String SUREFIRE_PLUGIN = "maven-surefire-plugin";
  private static final String FAILSAFE_PLUGIN = "maven-failsafe-plugin";
  private static final String[] SWT = "org.eclipse.platform:org.eclipse.swt".split(":");
  private static final String[] RWT = "org.eclipse.rap:org.eclipse.rwt".split(":");
  private static final String MAVEN_CONFIG = ".mvn/maven.config";

  private final Logger log = LoggerFactory.getLogger(FirstThreadOnMacExtension.class);
  private final String os = SwtPlatformExtension.normalizeOs(System.getProperty("os.name"));
  private Boolean withSwtDependency;
  private Path rootPath;
  private boolean inMvnConfig;

  @Override
  public void beforeMojoExecution(MojoExecutionEvent event) {
    MavenProject project = event.getProject();
    if (!("jar".equals(project.getPackaging()) && "osx".equals(os))) {
      return;
    }
    if (isSurefireMojo(event.getExecution()) && withSwtDependency == null) {
      withSwtDependency = project.getDependencies().stream().anyMatch(this::isUiDependency);
    }
    if (Boolean.TRUE.equals(withSwtDependency) && injectJvmArgToArgLine(event.getMojo())) {
      log.info("{}", message(project.getArtifact()));
      if (inMvnConfig) {
        return;
      }
      if (rootPath == null) {
        rootPath = getRootPath(project.getBasedir().toPath());
      }
      String value = String.format("-DargLine=%s", START_ON_FIRST_THREAD_ARG.trim());
      Path mvnCfg = rootPath.resolve(MAVEN_CONFIG);
      inMvnConfig = existStartOnFirstThreadArg(mvnCfg, value);
      if (!inMvnConfig) {
        injectToMvnConfig(mvnCfg, value);
      }
    }
  }

  boolean isSurefireMojo(MojoExecution execution) {
    String goal = execution.getGoal();
    String artifactId = execution.getArtifactId();
    return (SUREFIRE_PLUGIN.equals(artifactId) && "test".equals(goal))
        || FAILSAFE_PLUGIN.equals(artifactId) && "integration-test".equals(goal);
  }

  boolean injectJvmArgToArgLine(Mojo mojo) {
    Class<?> clazz = mojo.getClass();
    try {
      clazz
          .getMethod("setArgLine", String.class)
          .invoke(mojo, newArgLine((String) clazz.getMethod("getArgLine").invoke(mojo)));
      return true;
    } catch (ReflectiveOperationException ex) {
      log.warn("Try inject JVM arg line to arg line failed: {}", ex.getMessage());
    }
    return false;
  }

  private String newArgLine(String argLine) {
    return START_ON_FIRST_THREAD_ARG + (argLine != null ? argLine : "");
  }

  @Override
  public void afterMojoExecutionSuccess(MojoExecutionEvent event) {
    // ignore
  }

  @Override
  public void afterExecutionFailure(MojoExecutionEvent event) {
    // ignore
  }

  private boolean isUiDependency(Dependency dependency) {
    String groupId = dependency.getGroupId();
    String artifactId = dependency.getArtifactId();
    return HOP_GROUP_ID.equals(groupId)
            && (HOP_TESTING_ARTIFACT_ID.equals(artifactId) || "hop-ui".equals(artifactId))
        || SWT[0].equals(groupId) && SWT[1].equals(artifactId)
        || RWT[0].equals(groupId) && RWT[1].equals(artifactId);
  }

  private MessageBuilder message(Artifact artifact) {
    return MessageUtils.buffer()
        .a("Testing ")
        .project(artifact.getArtifactId())
        .a(" module, apply jvm jvmArg:")
        .success(START_ON_FIRST_THREAD_ARG.trim());
  }

  private Path getRootPath(Path modulePath) {
    List<Path> paths = new ArrayList<>();
    Path path = modulePath;
    int count = 0;
    while (path.getNameCount() > 1) {
      if (Files.exists(path.resolve("pom.xml"))) {
        paths.add(path);
      } else {
        count++;
      }
      if (count > 2 || Files.exists(path.resolve(".git"))) {
        break;
      }
      path = path.getParent();
    }
    return paths.isEmpty() ? modulePath : paths.get(paths.size() - 1);
  }

  private boolean existStartOnFirstThreadArg(Path mavenConfig, String value) {
    if (Files.exists(mavenConfig)) {
      try {
        return Files.readString(mavenConfig).contains(value);
      } catch (IOException ignore) {
      }
    }
    return false;
  }

  private void injectToMvnConfig(Path mavenConfig, String value) {
    try {
      if (!Files.exists(mavenConfig.getParent())) {
        Files.createDirectory(mavenConfig.getParent());
      }
      List<String> lines =
          Files.exists(mavenConfig) ? Files.readAllLines(mavenConfig) : new ArrayList<>();
      if (lines.isEmpty()) {
        lines.add("\n");
      }
      lines.add(0, value);
      Files.write(mavenConfig, String.join("\n", lines).getBytes());
    } catch (IOException ignore) {
      // Ignore
    }
  }
}
