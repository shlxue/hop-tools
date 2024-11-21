package org.apache.hop.testing.extension;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.testing.junit.HopHelper;
import org.apache.hop.testing.junit.HopJunit;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.testing.junit.SwtContext;
import org.apache.hop.testing.params.ParameterResolvers;
import org.apache.hop.testing.ui.UiSpecs;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class PluginUiTestTemplateExtension implements TestTemplateInvocationContextProvider {

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    Method testMethod = context.getRequiredTestMethod();
    if (testMethod.getParameterCount() == 1) {
      Class<?> paramType = testMethod.getParameterTypes()[0];
      if (HopHelper.isPluginUi(paramType)) {
        Preconditions.condition(
            AnnotationUtils.isAnnotated(testMethod, TestTemplate.class),
            () -> "Not found @TestTemplate annotation on test method " + testMethod.getName());
        Class<?>[] pluginTypes = tryToPluginType(testMethod).stream().toArray(Class[]::new);
        StatusUtil.set(context, StoreKey.HOP_JUNIT_PLUGINS, pluginTypes);
        StatusUtil.set(context, StoreKey.HOP_JUNIT_PLUGIN_METAS, tryToPluginMetaType(pluginTypes));
        return true;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    HopEnv hopEnv = context.getRequiredTestClass().getAnnotation(HopEnv.class);
    HopJunit hopJunit = StatusUtil.get(context, StoreKey.HOP_JUNIT, HopJunit.class);
    Class<?> paramType = context.getRequiredTestMethod().getParameterTypes()[0];
    ParameterResolver rawParamResolver = pluginUiParamResolver(paramType);
    SpecMode specMode = StatusUtil.uiSpec(hopEnv);
    StatusUtil.set(context, StoreKey.HOP_JUNIT_SPEC, specMode);
    UiSpecs<Dialog> specs = UiSpecs.builder(newShell(context), (Class<Dialog>) paramType);
    return Stream.of(templateContexts(specMode, specs, rawParamResolver));
  }

  private TestTemplateInvocationContext[] templateContexts(
      SpecMode specMode, UiSpecs<?> uiSpecs, ParameterResolver root) {
    List<TestTemplateInvocationContext> contexts = new ArrayList<>();

    contexts.add(newContext("originTestMethod", root));
    if (specMode.isTest()) {
      contexts.add(newContext("assertDialog: initInConstructor", root, uiSpecs.buildUi().build()));
      contexts.add(newContext("assertShell: preferredSize", root, uiSpecs.minimum().build()));
      contexts.add(newContext("assertShell: autoLayout", root, uiSpecs.checkAutoLayout().build()));
    }
    if (specMode.isTest() && !specMode.isHeadless() || specMode.isColorized()) {
      List<String> mixNames = new ArrayList<>(4);
      if (specMode.isColorized()) {
        mixNames.add("colorized");
        uiSpecs.colorized();
      }
      mixNames.addAll(Arrays.asList("tags", "tabOrder", "autoLayout"));
      uiSpecs.tags().tabOrder().previewAutoLayout().delayClose();
      contexts.add(
          newContext("assertShell: " + String.join(" & ", mixNames), root, uiSpecs.build()));
    }
    if (specMode.isTest()) {
      contexts.add(newContext("assertShell: defaultButton", root, uiSpecs.defaultButton().build()));
      contexts.add(
          newContext("assertShell: lazyApplyTheme", root, uiSpecs.lazyApplyTheme().build()));
      contexts.add(
          newContext("assertListener: switchFocus", root, uiSpecs.tags().switchFocus().build()));
      if (specMode.isStaticEvent()) {
        contexts.add(newContext("assertListener: ok", root, uiSpecs.okListener().build()));
      }
      if (specMode.isDynamicEvent()) {
        // TODO test more custom listener
        // contexts.add(newContext("assertListener: fillValue", root, uiSpecs.build()));
      }
    }
    return contexts.toArray(new TestTemplateInvocationContext[0]);
  }

  private TestTemplateInvocationContext newContext(
      String name, ParameterResolver paramResolver, InvocationInterceptor... specs) {
    return TestContexts.ofUi(name, paramResolver, specs);
  }

  private ParameterResolver pluginUiParamResolver(Class<?> paramType) {
    return HopHelper.isActionUi(paramType)
        ? ParameterResolvers.actionUi()
        : ParameterResolvers.transformUi();
  }

  private Optional<Class<?>> tryToPluginType(Method testMethod) {
    if (isTestMethodWithParam(testMethod)) {
      String pluginType = testMethod.getParameterTypes()[0].getName().replaceFirst("Dialog$", "");
      pluginType = pluginType.replaceFirst("\\.ui\\.", ".");
      return ReflectionUtils.tryToLoadClass(pluginType, getClass().getClassLoader()).toOptional();
    }
    return Optional.empty();
  }

  private boolean isTestMethodWithParam(Method testMethod) {
    return testMethod.getParameterCount() == 1
        && (testMethod.getAnnotation(TestTemplate.class) != null);
  }

  private Class<?>[] tryToPluginMetaType(Class<?>[] pluginType) {
    return Arrays.stream(pluginType)
        .map(clazz -> clazz.getName() + "Meta")
        .map(name -> ReflectionUtils.tryToLoadClass(name, getClass().getClassLoader()).toOptional())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toArray(Class[]::new);
  }

  private Shell newShell(ExtensionContext context) {
    return StatusUtil.get(
        context, StoreKey.HOP_SWT_NEW_SHELL, Shell.class, k -> SwtContext.getInstance().newShell());
  }
}
