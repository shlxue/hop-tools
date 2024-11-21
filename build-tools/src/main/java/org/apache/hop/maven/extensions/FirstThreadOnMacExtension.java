package org.apache.hop.maven.extensions;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.ProjectExecutionEvent;
import org.apache.maven.execution.ProjectExecutionListener;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Component(role = ProjectExecutionListener.class, hint = "first-thread-on-mac")
public class FirstThreadOnMacExtension implements ProjectExecutionListener {
  private static final String START_ON_FIRST_THREAD_ARG = " -XstartOnFirstThread";
  private static final String HOP_GROUP_ID = "org.apache.hop";
  private static final String HOP_TESTING_ARTIFACT_ID = "hop-plugin-testing";

  private static final String MAVEN_GROUP_ID = "org.apache.maven.plugins";
  private static final String SUREFIRE_PLUGIN = "maven-surefire-plugin";
  private static final String FAILSAFE_PLUGIN = "maven-failsafe-plugin";
  private final Logger log = LoggerFactory.getLogger(FirstThreadOnMacExtension.class);

  @Override
  public void beforeProjectExecution(ProjectExecutionEvent event) {
    // ignore
  }

  @Override
  public void beforeProjectLifecycleExecution(ProjectExecutionEvent event) {
    MavenProject project = event.getProject();
    if (event.getExecutionPlan() == null
        || !"jar".equals(project.getPackaging())
        || !"osx".equals(SwtPlatformExtension.normalizeOs(System.getProperty("os.name")))
        || event.getExecutionPlan().stream().noneMatch(this::withTest)
        || project.getDependencies().stream().noneMatch(this::isUiDependency)) {
      return;
    }
    Properties props = new Properties();
    props.putAll(event.getSession().getSystemProperties());
    props.putAll(event.getSession().getUserProperties());
    boolean inject = false;
    for (MojoExecution mojo : event.getExecutionPlan()) {
      if (withTest(mojo) && injectFirstThreadArg(mojo.getConfiguration(), props)) {
        inject = true;
      }
    }
    if (inject) {
      log.info(message(project.getArtifact(), START_ON_FIRST_THREAD_ARG));
    }
  }

  @Override
  public void afterProjectExecutionSuccess(ProjectExecutionEvent projectExecutionEvent) {
    // ignore
  }

  @Override
  public void afterProjectExecutionFailure(ProjectExecutionEvent projectExecutionEvent) {
    // ignore
  }

  private boolean injectFirstThreadArg(Object configuration, Properties props) {
    String key = "argLine";
    try {
      Xpp3DomAdapter node = Xpp3DomAdapter.of(configuration);
      Xpp3DomAdapter argLineNode;
      Object argLine = node.getChild(key);
      if (argLine != null) {
        argLineNode = Xpp3DomAdapter.of(argLine);
        String value = argLineNode.getValue();
        if (value != null) {
          argLineNode.setValue(resolve(value, props) + START_ON_FIRST_THREAD_ARG);
          return true;
        }
      } else {
        argLineNode = Xpp3DomAdapter.of(node.get().getClass(), key);
        argLineNode.setValue(START_ON_FIRST_THREAD_ARG);
        node.addChild(argLineNode.get());
        return true;
      }
    } catch (Exception e) {
      log.warn("Error while parsing argLine", e);
    }
    return false;
  }

  private String resolve(String argLine, Properties props) {
    Map<String, String> map = new LinkedHashMap<>();
    Matcher matcher = Pattern.compile("\\$\\{(\\S+)}").matcher(argLine);
    while (matcher.find()) {
      String key = matcher.group(1);
      String value = key != null ? props.getProperty(key) : "";
      map.put(matcher.group(0), value == null ? "" : value);
    }
    String[] keys = map.keySet().toArray(String[]::new);
    for (int i = keys.length - 1; i >= 0; i--) {
      String key = keys[i];
      argLine = argLine.replace(key, map.get(key));
    }
    return argLine;
  }

  private boolean isUiDependency(Dependency dependency) {
    String groupId = dependency.getGroupId();
    String artifactId = dependency.getArtifactId();
    return HOP_GROUP_ID.equals(groupId)
            && (HOP_TESTING_ARTIFACT_ID.equals(artifactId) || "hop-ui".equals(artifactId))
        || "org.eclipse.platform".equals(groupId) && "org.eclipse.swt".equals(artifactId)
        || "org.eclipse.rap".equals(groupId) && "org.eclipse.rwt".equals(artifactId);
  }

  private boolean withTest(MojoExecution mojo) {
    return isTestPlugin(mojo) && !"none".equals(mojo.getLifecyclePhase());
  }

  private boolean isTestPlugin(MojoExecution mojo) {
    Plugin plugin = mojo.getPlugin();
    String goal = mojo.getGoal();
    return MAVEN_GROUP_ID.equals(plugin.getGroupId())
        && (SUREFIRE_PLUGIN.equals(plugin.getArtifactId()) && "test".equals(goal)
            || FAILSAFE_PLUGIN.equals(plugin.getArtifactId()) && "integration-test".equals(goal));
  }

  private String message(Artifact artifact, String option) {
    return Buffers.builder()
        .a("Testing ")
        .project(artifact.getArtifactId())
        .a(" module, apply jvm option:")
        .success(option)
        .toString();
  }
}
