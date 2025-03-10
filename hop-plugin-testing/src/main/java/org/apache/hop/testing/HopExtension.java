package org.apache.hop.testing;

import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.testing.extension.TestTemplates;
import org.apache.hop.testing.junit.*;
import org.apache.hop.testing.tool.UiPreviewer;
import org.apache.hop.ui.util.HopHelper;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HopExtension
    implements BeforeAllCallback, AfterAllCallback, TestTemplateInvocationContextProvider {
  static final Logger logger = LoggerFactory.getLogger(HopExtension.class);

  private final TestTemplateInvocationContextProvider pipelineEngineProvider;
  private final TestTemplateInvocationContextProvider workflowEngineProvider;
  private final TestTemplateInvocationContextProvider pluginUiProvider;
  private SwtExtension swtExtension;
  private H2Extension h2Extension;

  public HopExtension() {
    this.pipelineEngineProvider = TestTemplates.pipelineEngineProvider();
    this.workflowEngineProvider = TestTemplates.workflowEngineProvider();
    this.pluginUiProvider = TestTemplates.pluginUiProvider();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    HopEnv hopEnv = testClass.getAnnotation(HopEnv.class);
    HopJunit hopJunit = new HopJunit(StatusUtil.envType(hopEnv), StatusUtil.withH2(hopEnv));
    if (StatusUtil.uiSpec(hopEnv).isTest()) {
      swtExtension = new SwtExtension();
      swtExtension.beforeAll(context);
    }
    boolean withH2 = StatusUtil.withH2(hopEnv);
    if (withH2) {
      h2Extension = new H2Extension();
      h2Extension.beforeAll(context);
    }
    StatusUtil.set(context, StoreKey.HOP_JUNIT, hopJunit);
    hopJunit.waitUntilLoaded();
    if (withH2) {
      DatabaseMeta databaseMeta =
          StatusUtil.get(context, StoreKey.HOP_H2_SERVER, H2Server.class).getDatabase();
      hopJunit.setup(DatabaseMeta.class, databaseMeta);
      logger.debug("Inject embedded database meta: " + databaseMeta);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    StatusUtil.remove(context, StoreKey.HOP_JUNIT, HopJunit.class).close();
    if (h2Extension != null) {
      h2Extension.afterAll(context);
    }
    if (swtExtension != null) {
      swtExtension.afterAll(context);
    }
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    Set<TestTemplateInvocationContextProvider> supportedProviders =
        providers()
            .filter(provider -> provider.supportsTestTemplate(context))
            .collect(Collectors.toSet());
    if (supportedProviders.isEmpty()) {
      logger.trace("Not match test templates");
      return false;
    }
    if (swtExtension != null) {
      StatusUtil.set(context, "hop.swtExtension", true);
    }
    StatusUtil.set(context, "context.providers", supportedProviders);
    return true;
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    Set<TestTemplateInvocationContextProvider> providerSet =
        StatusUtil.remove(context, "context.providers", Set.class);
    StatusUtil.get(context, StoreKey.HOP_JUNIT, HopJunit.class).waitUntilLoaded();
    return providerSet.stream()
        .flatMap(provider -> provider.provideTestTemplateInvocationContexts(context));
  }

  private Stream<TestTemplateInvocationContextProvider> providers() {
    Stream<TestTemplateInvocationContextProvider> providers =
        Stream.of(pipelineEngineProvider, workflowEngineProvider, pluginUiProvider);
    return swtExtension == null ? providers : Stream.concat(Stream.of(swtExtension), providers);
  }

  /// / Hop
  private <T> void previewDialog(Shell parent, Function<Shell, T> creator) {
    Shell[] before = parent.getShells();
    T dialog = creator.apply(parent);
    if (dialog instanceof Dialog) {
      UiPreviewer previewer = UiPreviewer.get(parent);
      previewer.preview(UiPreviewer.findEventLoopShell(parent, (Dialog) dialog, before), false);
    }
  }
}
