package org.apache.hop.testing.extension;

import org.apache.hop.core.IExtensionData;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.testing.junit.HopJunit;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.testing.params.provider.HopFilter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

abstract class BaseTestTemplateProvider<T extends IExtensionData>
    implements TestTemplateInvocationContextProvider {
  protected final Logger logger = LoggerFactory.getLogger(BaseTestTemplateProvider.class);

  private final Class<T> pluginType;
  protected final Class<?>[] request;
  protected final Class<?>[] optional;
  protected HopJunit hopJunit;
  protected Class<? extends IExtensionData> pluginClass;
  private Pattern[] filePattern = new Pattern[0];

  BaseTestTemplateProvider(Class<T> pluginType, Class<?>[] request, Class<?>... optional) {
    this.pluginType = pluginType;
    this.request = Stream.concat(Stream.of(request), Stream.of(pluginType)).toArray(Class[]::new);
    this.optional = optional;
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    hopJunit = StatusUtil.get(context, StoreKey.HOP_JUNIT, HopJunit.class);
    Method testMethod = context.getRequiredTestMethod();
    logger.trace(
        "Test method: {} for {} {}",
        Integer.toHexString(hashCode()),
        testMethod.getName(),
        getClass().getSimpleName());
    if (request.length == 0 || testMethod.getParameterCount() >= 1) {
      return matchParameters(context.getRequiredTestClass(), testMethod);
    }
    return false;
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    Method testMethod = context.getRequiredTestMethod();
    HopFilter hopFilter = testMethod.getAnnotation(HopFilter.class);
    if (hopFilter != null) {
      if (!IExtensionData.class.equals(hopFilter.value())) {
        pluginClass = hopFilter.value();
      }
      filePattern = Stream.of(hopFilter.files()).map(Pattern::compile).toArray(Pattern[]::new);
    }
    if (pluginClass == null) {
      pluginClass =
          tryToPluginType(context.getRequiredTestClass())
              .orElseGet(
                  () -> getPluginTypeFromParams(testMethod.getParameterTypes()).orElse(null));
    }
    if (pluginClass != null) {
//    Preconditions.notNull(pluginClass, "Plugin class cannot be null");
      StatusUtil.set(context, StoreKey.HOP_JUNIT_PLUGINS, pluginClass);
    }
    return buildContexts(context, testMethod.getParameterTypes());
  }

  protected boolean matchParameters(Class<?> testClass, Method testMethod) {
    Class<?>[] params = testMethod.getParameterTypes();
    return Stream.of(params).anyMatch(clazz -> hasSupportType(request, clazz))
        && Stream.of(params)
        .allMatch(c -> hasSupportType(request, c) || hasSupportType(optional, c));
  }

  protected boolean hasSupportType(Class<?>[] types, Class<?> paramType) {
    return Stream.of(types).anyMatch(type -> type.isAssignableFrom(paramType));
  }

  private Optional<Class<T>> tryToPluginType(Class<?> testClass) {
    String className = testClass.getName().replaceFirst("(Test|IT)$", "");
    Optional<Class<?>> optional =
        ReflectionUtils.tryToLoadClass(className, getClass().getClassLoader()).toOptional();
    if (optional.isPresent() && pluginType.isAssignableFrom(optional.get())) {
      return Optional.of((Class<T>) optional.get());
    }
    return Optional.empty();
  }

  protected abstract String getPluginId(Class<?> pluginClass);

  protected abstract Stream<TestTemplateInvocationContext> buildContexts(
      ExtensionContext context, Class<?>... paramTypes);

  protected boolean filterProject(String name) {
    return true;
  }

  protected boolean filterEnv(String env) {
    return true;
  }

  protected boolean filterIds(String... ids) {
    String pluginId = getPluginId(getClass());
    return true;
  }

  protected boolean filterNames(Path path) {
    String fileName = path.getFileName().toString();
    return Stream.of(filePattern).anyMatch(pattern -> pattern.matcher(fileName).matches());
  }

  protected boolean filterPlugins(String id, String... names) {
    String pluginId = getPluginId(pluginClass);
    if (!StringUtil.isEmpty(pluginId)) {
      return pluginId.equals(id) && !id.startsWith("Beam");
    }
    return false;
  }

  private Optional<Class<T>> getPluginTypeFromParams(Class<?>[] classes) {
    return Stream.of(classes)
        .filter(aClass -> pluginType.isAssignableFrom(aClass))
        .map(aClass -> (Class<T>) aClass)
        .findFirst();
  }
}
