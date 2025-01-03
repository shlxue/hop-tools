package org.apache.hop.testing.extension;

import org.apache.hop.testing.HopEnv;
import org.apache.hop.testing.SpecMode;
import org.apache.hop.testing.junit.HopUiHelper;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.testing.junit.SwtContext;
import org.apache.hop.testing.params.ParameterResolvers;
import org.apache.hop.testing.ui.UiSpecs;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class SwtTestTemplateExtension implements TestTemplateInvocationContextProvider {

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    Method testMethod = context.getRequiredTestMethod();
    if (testMethod.getParameterCount() != 1) {
      return false;
    }
    Class<?> paramType = testMethod.getParameterTypes()[0];
    if (!HopUiHelper.isSwtWidget(paramType) && !HopUiHelper.isSwtDialog(paramType)) {
      return false;
    }
    Preconditions.condition(
        AnnotationUtils.isAnnotated(testMethod, TestTemplate.class),
        () -> "Not found @TestTemplate annotation on test method " + testMethod.getName());
    return !HopUiHelper.isPluginDialog(paramType);
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    Class<?> paramType = context.getRequiredTestMethod().getParameterTypes()[0];
    ParameterResolver rawParamResolver = swtParamResolver(paramType);
    SpecMode specMode =
        StatusUtil.uiSpec(context.getRequiredTestClass().getAnnotation(HopEnv.class));
    UiSpecs<?> specs = UiSpecs.builder(newShell(context), paramType);
    return Stream.of(buildContexts(specMode, specs, rawParamResolver));
  }

  private TestTemplateInvocationContext[] buildContexts(
      SpecMode specMode, UiSpecs<?> specs, ParameterResolver root) {
    List<TestTemplateInvocationContext> contexts = new ArrayList<>();
    contexts.add(newContext("createWidget", root));
    if (specMode.isTest()) {
      if (specMode.isHeadless()) {}
      //        newContext("createWidget", root, specs)
      //        TestContexts.ofSwt("previewWidget", root, specs),
      //        TestContexts.ofSwt("autoLayout", root, specs)
    }
    return contexts.toArray(new TestTemplateInvocationContext[0]);
  }

  private TestTemplateInvocationContext newContext(
      String name, ParameterResolver rawParamResolver, InvocationInterceptor... specs) {
    return TestContexts.ofSwt(name, rawParamResolver, specs);
  }

  private Shell newShell(ExtensionContext context) {
    return StatusUtil.get(
        context, StoreKey.HOP_SWT_NEW_SHELL, Shell.class, k -> SwtContext.getInstance().newShell());
  }

  private ParameterResolver swtParamResolver(Class<?> paramType) {
    return HopUiHelper.isSwtWidget(paramType)
        ? ParameterResolvers.swtWidget()
        : ParameterResolvers.swtDialog();
  }
}
