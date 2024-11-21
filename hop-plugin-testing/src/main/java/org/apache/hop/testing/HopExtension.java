package org.apache.hop.testing;

import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.testing.extension.TestTemplates;
import org.apache.hop.testing.junit.*;
import org.apache.hop.testing.tool.UiPreviewer;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class HopExtension
    implements BeforeAllCallback, AfterAllCallback, TestTemplateInvocationContextProvider {
  static final Logger logger = LoggerFactory.getLogger(HopExtension.class);

  private final TestTemplateInvocationContextProvider engineDelegate;
  private final TestTemplateInvocationContextProvider pluginDelegate;
  private final TestTemplateInvocationContextProvider pluginUiDelegate;

  private SwtExtension swtExtension;
  private H2Extension h2Extension;

  public HopExtension() {
    this.engineDelegate = TestTemplates.engineProvider();
    this.pluginDelegate = TestTemplates.pluginProvider();
    this.pluginUiDelegate = TestTemplates.pluginUiProvider();
    logger.trace("HopExtension created");
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    logger.trace("HopExtension before all");
    Class<?> testClass = context.getRequiredTestClass();
    HopEnv hopEnv = testClass.getAnnotation(HopEnv.class);

    if (StatusUtil.uiSpec(hopEnv).isTest()) {
      swtExtension = new SwtExtension();
      swtExtension.beforeAll(context);
    }
    boolean withH2 = StatusUtil.withH2(hopEnv);
    if (withH2) {
      h2Extension = new H2Extension();
      h2Extension.beforeAll(context);
    }
    HopJunit hopJunit = new HopJunit(StatusUtil.envType(hopEnv), withH2);
    StatusUtil.set(context, StoreKey.HOP_JUNIT, hopJunit);
    if (withH2) {
      hopJunit.waitUntilLoaded();
      DatabaseMeta databaseMeta =
          StatusUtil.get(context, StoreKey.HOP_H2_SERVER, H2Server.class).getDatabase();
      hopJunit.setup(DatabaseMeta.class, databaseMeta);
    }
    logger.debug("Auto config hop environment...");
  }

  @Override
  public void afterAll(ExtensionContext context) {
    logger.trace("HopExtension after all");
    StatusUtil.remove(context, StoreKey.HOP_JUNIT, HopJunit.class).close();
    if (h2Extension != null) {
      h2Extension.afterAll(context);
    }
    if (swtExtension != null) {
      swtExtension.afterAll(context);
    }
    logger.debug("Hop environment was shutdown");
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    Optional<TestTemplateInvocationContextProvider> optional =
        matchTestTemplate(context.getRequiredTestMethod());
    if (optional.isPresent() && optional.get().supportsTestTemplate(context)) {
      return true;
    }
    if (swtExtension != null && swtExtension.supportsTestTemplate(context)) {
      StatusUtil.set(context, "hop.swtExtension", true);
      return true;
    }
    return false;
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    if (StatusUtil.remove(context, "hop.swtExtension") != null) {
      return swtExtension.provideTestTemplateInvocationContexts(context);
    }
    StatusUtil.get(context, StoreKey.HOP_JUNIT, HopJunit.class).waitUntilLoaded();
    return matchTestTemplate(context.getRequiredTestMethod()).stream()
        .flatMap(provider -> provider.provideTestTemplateInvocationContexts(context));
  }

  private Optional<TestTemplateInvocationContextProvider> matchTestTemplate(Method testMethod) {
    if (testMethod.getParameterCount() == 1) {
      Class<?> parameterType = testMethod.getParameterTypes()[0];
      if (HopHelper.isPluginUi(parameterType)) {
        return Optional.of(pluginUiDelegate);
      }
      if (HopHelper.isEngine(parameterType)) {
        return Optional.of(engineDelegate);
      }
      if (HopHelper.isPlugin(parameterType)) {
        return Optional.of(pluginDelegate);
      }
    }
    return Optional.empty();
  }

  //// Hop
  private <T> void previewDialog(Shell parent, Function<Shell, T> creator) {
    Shell[] before = parent.getShells();
    T dialog = creator.apply(parent);
    if (dialog instanceof Dialog) {
      logger.info("Preview swt dialog");
      UiPreviewer previewer = UiPreviewer.get(parent);
      previewer.preview(UiPreviewer.findEventLoopShell(parent, (Dialog) dialog, before), false);
    }
  }
}
