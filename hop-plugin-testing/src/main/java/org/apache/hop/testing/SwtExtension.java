package org.apache.hop.testing;

import org.apache.hop.testing.extension.TestTemplates;
import org.apache.hop.testing.junit.StatusUtil;
import org.apache.hop.testing.junit.StoreKey;
import org.apache.hop.testing.junit.SwtContext;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

public final class SwtExtension
    implements BeforeAllCallback, AfterAllCallback, TestTemplateInvocationContextProvider {
  private static final Logger logger = LoggerFactory.getLogger(SwtExtension.class);

  private final SwtContext swtContext;
  private final TestTemplateInvocationContextProvider delegate;

  public SwtExtension() {
    logger.trace("SwtExtension created");
    this.swtContext = SwtContext.getInstance();
    this.delegate = TestTemplates.swtUiProvider();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    logger.trace("SwtExtension before all");
    if (supportUi(context.getRequiredTestClass())) {
      StatusUtil.set(context, StoreKey.HOP_SWT_CONTEXT, swtContext);
      Shell shell = swtContext.getShell();
      StatusUtil.set(context, StoreKey.HOP_SWT_SHELL, shell);
      injectShellField(context.getRequiredTestClass(), shell);
      logger.trace("Inject swt to junit store");
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    logger.trace("SwtExtension after all");
    StatusUtil.remove(context, StoreKey.HOP_SWT_CONTEXT);
    StatusUtil.remove(context, StoreKey.HOP_SWT_SHELL);
    logger.trace("Clean junit store for swt");
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return delegate.supportsTestTemplate(context);
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    return delegate.provideTestTemplateInvocationContexts(context);
  }

  private boolean supportUi(Class<?> testClass) {
    SpecMode specMode = StatusUtil.uiSpec(testClass.getAnnotation(HopEnv.class));
    if (swtContext.supportX11()) {
      return specMode != SpecMode.NONE;
    }
    return swtContext.supportX11() && specMode != SpecMode.NONE;
  }

  private boolean isShellField(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isStatic(modifiers)
        && !Modifier.isFinal(modifiers)
        && field.getType().equals(Shell.class);
  }

  private void injectShellField(Class<?> testClass, Shell shell) {
    for (Field field : testClass.getDeclaredFields()) {
      if (isShellField(field)) {
        try {
          ReflectionUtils.makeAccessible(field);
          field.set(null, shell);
        } catch (Throwable throwable) {
          logger.info("Ignore field {}", field, throwable);
        }
      }
    }
  }
}
